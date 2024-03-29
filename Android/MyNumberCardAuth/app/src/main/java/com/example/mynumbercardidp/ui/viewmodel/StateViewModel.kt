package com.example.mynumbercardidp.ui.viewmodel

import android.nfc.TagLostException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mynumbercardidp.ApduCommands
import com.example.mynumbercardidp.KeycloakConnectionApplication
import com.example.mynumbercardidp.data.HttPStatusCode
import com.example.mynumbercardidp.data.KeycloakRepository
import com.example.mynumbercardidp.data.Rfc3447HashPrefix
import com.example.mynumbercardidp.ui.ExternalUrls
import com.example.mynumbercardidp.ui.NfcState
import com.example.mynumbercardidp.ui.KeycloakState
import com.example.mynumbercardidp.ui.UiState
import com.example.mynumbercardidp.ui.ScreenModeState
import com.example.mynumbercardidp.ui.UriParameters
import com.example.mynumbercardidp.util.hexToByteArray
import com.example.mynumbercardidp.util.mynumber.APDUException
import com.example.mynumbercardidp.util.mynumber.NfcReader
import com.example.mynumbercardidp.util.toHexString
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.JWKMatcher
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.Base64
import java.util.Date


class StateViewModel(
    private val keycloakRepository: KeycloakRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var keycloakState: KeycloakState by mutableStateOf(KeycloakState.Loading)
        private set
    init {
    }

    companion object {
        const val logTag = "StateViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as KeycloakConnectionApplication)
                val keycloakRepository = application.container.keycloakRepository
                StateViewModel(keycloakRepository = keycloakRepository)
            }
        }
    }

    data class NfcResult(
        val status: NfcState? = null,
        val retData: ByteArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NfcResult

            if (!retData.contentEquals(other.retData)) return false

            return true
        }

        override fun hashCode(): Int {
            return retData.contentHashCode()
        }
    }

    // 現状androidTest内でモック化をできる手段が無いため、myNumberCardAuth()とその他プライベートメソッドは未カバレッジ
    private fun authenticate(url: String, mode: String, certificate: String, applicantData: String, sign: String) {

        viewModelScope.launch {
            keycloakState = KeycloakState.Loading
            keycloakState = try {
                val requestResult = if(_uiState.value.screenMode == ScreenModeState.UserCertRead) {
                    keycloakRepository.jpkiAuthenticate(
                        url,
                        mode,
                        certificate,
                        applicantData,
                        sign
                    )
                } else {
                    keycloakRepository.jpkiSignAuthenticate(
                        url,
                        mode,
                        certificate,
                        applicantData,
                        sign
                    )
                }

                Log.i(logTag, "Keycloak return code: ${requestResult.code()}")

                if (requestResult.code() == HttPStatusCode.Found.value){

                    val location = requestResult.headers()["Location"]

                    KeycloakState.Success(location!!)

                } else {
                    when(requestResult.code())
                    {
                        HttPStatusCode.BadRequest.value,HttPStatusCode.InternalServerError.value,HttPStatusCode.ServiceUnavailable.value -> KeycloakState.Error
                        HttPStatusCode.NotFound.value -> KeycloakState.UnRegisterError
                        HttPStatusCode.Unauthorized.value -> KeycloakState.LapseError
                        HttPStatusCode.Conflict.value -> KeycloakState.UserDuplicateError
                        HttPStatusCode.Gone.value -> {
                            val xActionUrl = requestResult.headers()["X-Action-URL"]
                            setUriParameters(
                                _uiState.value.uriParameters?.nonce!!,
                                "replacement",
                                xActionUrl!!,
                                _uiState.value.uriParameters?.error_url!!
                            )
                            KeycloakState.InfoChangeError
                        }
                        else -> KeycloakState.Error
                    }
                }
            } catch (e: IOException) {
                Log.e(logTag, "IOException occurred. Stack cause: ${e.message}")
                updateProgressViewState(false)
                KeycloakState.Error
            } catch (e: HttpException) {
                Log.e(logTag, "HttpException occurred. Stack cause: ${e.message}")
                updateProgressViewState(false)
                KeycloakState.Error
            }
            _uiState.update { _uiState.value.copy(keycloakState = keycloakState) }
        }

    }

    private fun setUriParameters(nonce: String, mode: String, actionUrl: String, errorUrl: String){
        var uriParameters = UriParameters(nonce, mode, actionUrl, errorUrl)
        viewModelScope.launch {
            _uiState.update { _uiState.value.copy(uriParameters = uriParameters) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun myNumberCardAuth(reader: NfcReader, inputPin: String) {
        try {
            var readCertificateResult = readCertificate(reader, inputPin)
            if (readCertificateResult.status != NfcState.Success){
                setState(KeycloakState.Error, readCertificateResult.status)
                updateProgressViewState(false)
                return
            }

            var signCertificateResult = computeSignature(reader, inputPin)
            if (signCertificateResult.status != NfcState.Success){
                setState(KeycloakState.Error, signCertificateResult.status)
                updateProgressViewState(false)
                return
            }

            val url = _uiState.value.uriParameters?.action_url!!
            val mode = URLEncoder.encode(_uiState.value.uriParameters?.mode!!, "UTF-8")

            // 証明書データ(DER形式)をPEM形式に変換
            val certificate = readCertificateResult?.retData!!
            val base64Certificate = Base64.getEncoder().encodeToString(certificate)
            val pemCertificate = "-----BEGIN CERTIFICATE-----\n$base64Certificate\n-----END CERTIFICATE-----"
            // Keycloakから渡されたactionUrlからjwksの配置URLを生成
            val regex = "^https?://[^/]+/realms/[^/]+".toRegex()
            val jwksUrl = regex.find(url)?.value + "/protocol/openid-connect/certs"
            val jweCertificate = encrypt(pemCertificate, jwksUrl)

            val applicantData = _uiState.value.uriParameters?.nonce!!
            val sign = Base64.getEncoder().encodeToString(signCertificateResult?.retData)

            authenticate(url, mode, jweCertificate, applicantData, sign)

        } catch (e: APDUException) {
            Log.e(logTag, "APDUException occurred. cause: ${e.message}")
            setState(KeycloakState.Error,NfcState.Failure)
            updateProgressViewState(false)
        }
        catch (e: TagLostException) {
            Log.e(logTag, "TagLostException occurred. cause: ${e.message}")
            setState(KeycloakState.Error,NfcState.Failure)
            updateProgressViewState(false)
        } catch (e: SecurityException) {
            Log.e(logTag, "SecurityException occurred. cause: ${e.message}")
            setState(KeycloakState.Error,NfcState.Failure)
            updateProgressViewState(false)
        }
        catch (e: Exception) {
            Log.e(logTag, "Exception occurred. Stack cause: ${e.message}")
            setState(KeycloakState.Error)
            updateProgressViewState(false)
        }
    }

    fun updateProgressViewState(isView: Boolean, title: String = "", message: String = "") {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    isNfcReading = isView,
                    nfcReadingTitle = title,
                    nfcReadingMessage = message,
                )
            }
        }
    }

    fun setState(keycloakState: KeycloakState, nfcState: NfcState? = NfcState.None){
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(keycloakState = keycloakState, nfcState = nfcState)
            }
        }
    }
    fun changeViewMode(screenMode: ScreenModeState){
        viewModelScope.launch {
            _uiState.update { _uiState.value.copy(screenMode = screenMode) }
        }
    }

    fun setUriParameters(uriParameters: UriParameters?){
        viewModelScope.launch {
            _uiState.update { _uiState.value.copy(uriParameters = uriParameters) }
        }
    }

    fun setExternalUrls(externalUrls: ExternalUrls){
        viewModelScope.launch {
            _uiState.update { _uiState.value.copy(externalUrls = externalUrls) }
        }
    }

    private fun encrypt(content: String, jwksUrl: String): String {
        val now = Date()

        // JWT作成
        val jwtClaims = JWTClaimsSet.Builder()
            .claim("claim", content)
            .expirationTime(Date(now.time + 1000 * 60 * 5)) // expires in 5 minutes
            .build()
        val header = JWEHeader(
            JWEAlgorithm.RSA_OAEP_256,
            EncryptionMethod.A128CBC_HS256
        )
        val jwt = EncryptedJWT(header, jwtClaims)

        // 公開鍵を取得
        val jwkSet = JWKSet.load(URL(jwksUrl))
        val matches = JWKSelector(JWKMatcher.forJWEHeader(header)).select(jwkSet)
        val publicKey = matches[0].toRSAKey()
        val encrypter = RSAEncrypter(publicKey)

        // 暗号化に使用した共通鍵を、公開鍵を用いて暗号化
        jwt.encrypt(encrypter)

        return jwt.serialize()
    }

    private fun readCertificate(reader: NfcReader, inputPin: String): StateViewModel.NfcResult {
        var result = if (_uiState.value.screenMode == ScreenModeState.UserCertRead){
            readCertificateUserVerification(reader, inputPin)
        } else {
            readCertificateSign(reader, inputPin)
        }

        return result
    }

    private fun readCertificateUserVerification(reader: NfcReader, inputPin: String): StateViewModel.NfcResult {
        val jpki = reader.selectJpki(ApduCommands.jpkiAp)

        // 認証用証明書取得
        val retData = jpki.readCertificate(ApduCommands.userAuthenticationCertificate)
        Log.d(logTag, "retData: ${retData.toHexString()}")

        return NfcResult(NfcState.Success, retData)
    }

    private fun readCertificateSign(reader: NfcReader, inputPin: String): NfcResult {
        // AP選択
        val jpki = reader.selectJpki(ApduCommands.jpkiAp)

        // PINの残りカウント取得
        val count = jpki.lookupPin(ApduCommands.digitalSignaturePin)
        if (count == 0) {
            return NfcResult(NfcState.TryCountIsNotLeft, null)
        }

        // PIN解除
        if (!jpki.verifyPin(ApduCommands.digitalSignaturePin, inputPin)) {
            NfcResult(NfcState.IncorrectPin, null)
        }

        // 認証用証明書取得
        val retData = jpki.readCertificate(ApduCommands.digitalSignatureCertificate)
        Log.d(logTag, "retData: ${retData.toHexString()}")

        return NfcResult(NfcState.Success, retData)
    }


    private fun computeSignature(reader: NfcReader, inputPin: String): NfcResult {
        return if (_uiState.value.screenMode == ScreenModeState.UserCertRead){
            userCertComputeSignature(reader, inputPin)
        } else {
            signCertComputeSignature(reader, inputPin)
        }
    }

    private fun userCertComputeSignature(reader: NfcReader, inputPin: String): NfcResult {
        // AP選択
        val jpki = reader.selectJpki(ApduCommands.jpkiAp)

        // PINの残りカウント取得
        val count = jpki.lookupPin(ApduCommands.userAuthenticationPin)
        if (count == 0) {
            return NfcResult(NfcState.TryCountIsNotLeft,null)
        }

        // PIN解除
        if (!jpki.verifyPin(ApduCommands.userAuthenticationPin, inputPin)) {
            return NfcResult(NfcState.IncorrectPin,null)
        }

        // 署名対象のデータをハッシュ化
        val digest = MessageDigest.getInstance("SHA-256").digest(_uiState.value.uriParameters?.nonce!!.toByteArray(Charset.defaultCharset()))
        Log.d(logTag, "nonceHash: ${digest.toHexString()}")

        //RSA署名のハッシュプレフィックス
        val hashPrefix = Rfc3447HashPrefix.SHA256.toString()
        // ハッシュ値をDigestInfo の形式に変換する
        val digestInfo = hashPrefix.hexToByteArray() + digest

        // カードの秘密鍵で署名する
        val signature = jpki.computeSignature(ApduCommands.userAuthenticationPrivate, ApduCommands.computeDigitalSignature, digestInfo)
        Log.d(logTag, "signature: ${signature.toHexString()}")

        return NfcResult(NfcState.Success, signature)
    }

    private fun signCertComputeSignature(reader: NfcReader, inputPin: String): NfcResult {
        // AP選択
        val jpki = reader.selectJpki(ApduCommands.jpkiAp)

        // PINの残りカウント取得
        val count = jpki.lookupPin(ApduCommands.digitalSignaturePin)
        if (count == 0) {
            return NfcResult(NfcState.TryCountIsNotLeft,null)
        }

// PIN解除
        if (!jpki.verifyPin(ApduCommands.digitalSignaturePin, inputPin)) {
            return NfcResult(NfcState.IncorrectPin,null)
        }

        // 署名対象のデータをハッシュ化
        val digest = MessageDigest.getInstance("SHA-256").digest(_uiState.value.uriParameters?.nonce!!.toByteArray(Charset.defaultCharset()))
        Log.d(logTag, "digest: ${digest.toHexString()}")

        //RSA署名のハッシュプレフィックス
        val hashPrefix = Rfc3447HashPrefix.SHA256.toString()
        // ハッシュ値をDigestInfo の形式に変換する
        val digestInfo = hashPrefix.hexToByteArray() + digest

        // カードの秘密鍵で署名する
        val signature = jpki.computeSignature(ApduCommands.digitalSignaturePrivate, ApduCommands.computeDigitalSignature, digestInfo)
        Log.d(logTag, "signature: ${signature.toHexString()}")

        return NfcResult(NfcState.Success, signature)
    }
}
