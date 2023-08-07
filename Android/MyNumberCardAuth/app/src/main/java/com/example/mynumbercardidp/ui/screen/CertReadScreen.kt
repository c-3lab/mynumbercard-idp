package com.example.mynumbercardidp.ui.screen

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynumbercardidp.util.PasswordVisualTransformationExcludesLast
import com.example.mynumbercardidp.MainActivity
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.util.onEach
import com.example.mynumbercardidp.ui.theme.MyNumberCardAuthTheme
import com.example.mynumbercardidp.ui.viewmodel.StateViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.io.IOException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalUriHandler
import com.example.mynumbercardidp.data.MaxInputText
import com.example.mynumbercardidp.data.URLTypes
import com.example.mynumbercardidp.data.ValidInputText
import com.example.mynumbercardidp.ui.NfcState
import com.example.mynumbercardidp.ui.KeycloakState
import com.example.mynumbercardidp.ui.KeycloakState.Success
import com.example.mynumbercardidp.ui.ScreenModeState
import com.example.mynumbercardidp.ui.UriParameters
import com.example.mynumbercardidp.util.mynumber.NfcReader

data class ErrorDialogDetail(
    val title: String,
    val message: String,
    var url: String? = null,
    var linqText: String? = null,
    var newScreenMode: ScreenModeState? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertReadScreen(
    nfcAdapter: NfcAdapter?,
    activity: MainActivity?,
    viewModel: StateViewModel
) {
    val logTag = "CertReadScreen"
    var inputPin by remember { mutableStateOf("") }
    var visualTransformation: VisualTransformation by remember {
        mutableStateOf(PasswordVisualTransformation())
    }
    val uriHandler = LocalUriHandler.current
    val receivedState by viewModel.uiState.collectAsState()

    // テキスト入力のマスク化
    LaunchedEffect(Unit) {
        var job: Job? = null
        snapshotFlow { inputPin }.onEach(initial = ""){ old, new ->
            job?.cancel()
            job = launch {
                if (old.length < new.length) {
                    // 入力された最後の文字だけマスクを外し、1秒後にマスクに戻す
                    visualTransformation = PasswordVisualTransformationExcludesLast()
                    delay(1000)
                    visualTransformation = PasswordVisualTransformation()
                } else {
                    visualTransformation = PasswordVisualTransformation()
                }
            }
        }.collect()
    }

    // NFCタグの読み取り処理を非同期で実行
    class MyReaderCallback : NfcAdapter.ReaderCallback {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTagDiscovered(tag: Tag) {

            val reader = NfcReader(tag)

            viewModel.onChangeProgressViewState(true)

            try {
                reader.connect()
            } catch (e: Exception) {
                Log.e(logTag, "Failed to reader connect. cause: ${e.message}")
                viewModel.onChangeProgressViewState(false)
                viewModel.setState(KeycloakState.Error)
                return
            }

            Thread.sleep(2000)

            viewModel.myNumberCardAuth(reader, inputPin)

            nfcAdapter?.disableReaderMode(activity)

            try {
                if (reader.isConnected()){
                    reader.close()
                }
            } catch (e: IOException) {
                Log.e(logTag, "Failed to reader close. cause: ${e.message}")
                viewModel.setState(KeycloakState.Error)
            }

            viewModel.onChangeProgressViewState(false)
        }
    }

    Column (
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        ){
        Text(stringResource(R.string.screen_title),modifier = Modifier.padding(50.dp))

        //操作手順
        Text(createOperationText(receivedState.screenMode))

        TextField(
            value = inputPin,
            onValueChange = {
                if (it.length <= getMaxInputLength(receivedState.screenMode)){
                    inputPin = it
                } },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.padding(50.dp),
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = getKeyBoardType(receivedState.screenMode)
            )
        )
        Button(
            onClick = {
                nfcAdapter?.enableReaderMode(
                    activity,
                    MyReaderCallback(),
                    NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
            },
            enabled = isReadingButtonEnabled(receivedState.screenMode, inputPin),
            modifier = Modifier.padding(10.dp)
        ) {
            Text(stringResource(R.string.start_reading))
        }
    }

    if (receivedState.isNfcReading){
        ShowProgress()
    }

    if (receivedState.keycloakState is Success){
        val successState = receivedState.keycloakState as Success
        uriHandler.openUri(successState.message)
        viewModel.setState(KeycloakState.Loading)
    } else if (receivedState.keycloakState is KeycloakState.Loading) {
        // 何もしない
    } else {
        // ViewModelの値でエラーメッセージを表示
        val errorDialogDetail = createErrorDialogDetail(
            receivedState.keycloakState,
            receivedState.nfcState!!,
            receivedState.screenMode,
            receivedState.uriParameters!!
        )
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                if (!errorDialogDetail.linqText.isNullOrEmpty()){
                    TextButton(
                        onClick = {
                            uriHandler.openUri(URLTypes.Inquiry.toString())
                            viewModel.setState(KeycloakState.Loading)
                        }
                    ) {
                        Text(errorDialogDetail.linqText!!)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!errorDialogDetail.url.isNullOrEmpty()){
                            uriHandler.openUri(errorDialogDetail.url!!)
                        }
                        if (errorDialogDetail.newScreenMode != null){
                            viewModel.changeViewMode(errorDialogDetail.newScreenMode!!)
                        }
                        viewModel.setState(KeycloakState.Loading)
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = {
                Text(errorDialogDetail.title)
            },
            text = {
                Text(errorDialogDetail.message)
            },
        )
    }
}

@Composable
private fun createErrorDialogDetail(
    keycloakState: KeycloakState, nfcState: NfcState, screenMode: ScreenModeState, uriParameters: UriParameters): ErrorDialogDetail {
    var title = ""
    var message = ""
    var url : String? = null
    var linqText: String? = null
    var newScreenMode: ScreenModeState? = null

    when(keycloakState){
        is KeycloakState.UnRegisterError -> {
            title = stringResource(R.string.auth_failure)
            message = stringResource(R.string.auth_failure_description)
            url = uriParameters?.error_url?.replace("&amp;", "&")
        }
        is KeycloakState.LapseError -> {
            title = stringResource(R.string.auth_failure)
            val lapseCert = if (screenMode == ScreenModeState.UserCertRead){
                stringResource(R.string.user_cert_lapse)
            }else {
                stringResource(R.string.sign_cert_lapse)
            }
            message = String.format("%s%s", lapseCert, stringResource(R.string.lapse_description))
            linqText = stringResource(R.string.contact_page)
            url = URLTypes.Inquiry.toString()
        }
        is KeycloakState.UserDuplicateError -> {
            title = stringResource(R.string.registration_failure)
            message = stringResource(R.string.registration_failure_description)
            url = uriParameters?.error_url?.replace("&amp;", "&")
        }
        is KeycloakState.InfoChangeError -> {
            title = stringResource(R.string.my_number_card_reload)
            message = stringResource(R.string.my_number_card_reload_description)
            newScreenMode = ScreenModeState.SignCertRead
        }
        else -> {
            title = stringResource(R.string.failure)
            message = when(nfcState){
                NfcState.IncorrectPin  -> {
                    stringResource(R.string.in_correct_pin)
                }
                NfcState.TryCountIsNotLeft  -> {
                    stringResource(R.string.try_count_Is_not_left)
                }
                NfcState.Failure  -> {
                    stringResource(R.string.my_number_card_read_failure)
                }
                else -> {
                    stringResource(R.string.failure_description)
                }
            }
        }
    }

    return ErrorDialogDetail(title, message, url, linqText, newScreenMode)
}

@Composable
private fun createOperationText(screenMode: ScreenModeState?):String {
    var format = "%s%s%s"
    var text: String
    if (screenMode == ScreenModeState.UserCertRead){
        text = String.format(
            format,
            stringResource(R.string.description_first_user_verification),
            stringResource(R.string.digital_cert_for_user_verification),
            stringResource(R.string.password_input_for_user_verification)
        )
    } else{
        text = String.format(
            format,
            stringResource(R.string.description_first_user_verification),
            stringResource(R.string.digital_cert_for_sign),
            stringResource(R.string.password_input_for_sign)
        )
    }
    return text
}

@Composable
private fun ShowProgress() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

private fun isReadingButtonEnabled(screenMode: ScreenModeState, input: String): Boolean {
    val validInputLength = if (screenMode == ScreenModeState.UserCertRead){
        ValidInputText.CertForUserVerification.length
    } else {
        ValidInputText.CertForSign.length
    }

    return (input.length >= validInputLength)
}

private fun getMaxInputLength(screenMode: ScreenModeState): Int {
    val maxInputLength = if (screenMode == ScreenModeState.UserCertRead){
        MaxInputText.CertForUserVerification.length
    } else {
        MaxInputText.CertForSign.length
    }
    return maxInputLength
}

private fun getKeyBoardType(screenMode: ScreenModeState): KeyboardType {
    val keyBoardType = if (screenMode == ScreenModeState.UserCertRead){
        KeyboardType.NumberPassword
    } else {
        KeyboardType.Password
    }
    return keyBoardType
}

@Preview(
    showBackground = true,
    widthDp = 320)
@Composable
fun MainScreenPreview() {
    MyNumberCardAuthTheme {
        CertReadScreen(null, null, viewModel(factory = StateViewModel.Factory))
    }
}
