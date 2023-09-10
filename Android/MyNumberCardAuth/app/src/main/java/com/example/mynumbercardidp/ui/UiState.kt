package com.example.mynumbercardidp.ui

data class UiState(
    var isNfcReading: Boolean = false,
    var keycloakState: KeycloakState = KeycloakState.Loading,
    var screenMode: ScreenModeState = ScreenModeState.ManualBoot,
    var uriParameters: UriParameters? = null,
    var nfcState: NfcState? = NfcState.None,
    var externalUrls: ExternalUrls? = null,
    var nfcReadingTitle: String = "",
    var nfcReadingMessage: String = "",
    var apduCommands: ApduCommands? = null,
) {
    companion object
    {
        val Initial = UiState()
    }
}

sealed interface KeycloakState {
    data class Success(val message: String) : KeycloakState // 成功
    object Error : KeycloakState // 失敗
    object UnRegisterError : KeycloakState // ユーザ未登録
    object LapseError : KeycloakState // 証明書の失効
    object UserDuplicateError : KeycloakState // ユーザの重複(登録済)
    object InfoChangeError : KeycloakState // ユーザ情報が変更されている
    object Loading : KeycloakState // 初期状態
}

enum class NfcState {
    Success,
    TryCountIsNotLeft,
    IncorrectPin,
    Failure,
    None,
}

data class UriParameters(
    var nonce: String? = "",
    val mode: String? = "",
    val action_url: String? = "",
    val error_url: String? = "",
)

enum class ScreenModeState {
    SignCertRead,
    UserCertRead,
    ManualBoot,
}

data class ExternalUrls(
    var inquiry: String? = "",
    val privacyPolicy: String? = "",
    val protectionPolicy: String? = "",
    val termsOfUse: String? = "",
)

data class ApduCommands(
    val selectJpkiAp: String? = "",
    val selectUserAuthenticationPin: String? = "",
    val selectDigitalSignaturePin: String? = "",
    val computeDigitalSignature: String? = "",
    val selectUserAuthenticationPrivate: String? = "",
    val selectDigitalSignaturePrivate: String? = "",
    val selectUserAuthentication: String? = "",
    val selectDigitalSignature: String? = "",
)
