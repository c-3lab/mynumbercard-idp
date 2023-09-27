package com.example.mynumbercardidp.ui.screen

import android.content.Context
import android.nfc.NfcAdapter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.ui.ExternalUrls
import com.example.mynumbercardidp.ui.KeycloakState
import com.example.mynumbercardidp.ui.NfcState
import com.example.mynumbercardidp.ui.ScreenModeState
import com.example.mynumbercardidp.ui.UriParameters
import com.example.mynumbercardidp.ui.viewmodel.StateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CertReadScreenTest {
    val appContext: Context = ApplicationProvider.getApplicationContext()
    var nfcAdapter: NfcAdapter? = null
    val title = appContext.getString(R.string.title)
    val screenTitle = appContext.getString(R.string.screen_title)
    val operationTextUser = appContext.getString(R.string.description_first_user_verification) +
            appContext.getString(R.string.digital_cert_for_user_verification) +
            appContext.getString(R.string.password_input_for_user_verification)
    val operationTextDigitalSignature = appContext.getString(R.string.description_first_user_verification) +
            appContext.getString(R.string.digital_cert_for_sign) +
            appContext.getString(R.string.password_input_for_sign)
    val startReadingButtonText = appContext.getString(R.string.start_reading)
    val labelPassword = appContext.getString(R.string.password)
    val scanTitleReady = appContext.getString(R.string.scan_title_ready)

    private lateinit var stateViewModel: StateViewModel
    @Before
    fun setUp() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(appContext)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createExternalUls(): ExternalUrls {
        return ExternalUrls(
            "https://dummy",
            "https://example.com/open-id/privacy-policy.html",
            "https://example.com/open-id/personal-data-protection-policy.html",
            "https://example.com/open-id/terms-of-use.html"
        )
    }

    private fun createUriParameters(mode:String = "", actionUrl: String = "", errorUrl:String = ""): UriParameters {
        return UriParameters(
            "nonce",
            mode,
            actionUrl,
            errorUrl
        )
    }

    private fun ComposeContentTestRule.assertAlertDialogIsDisplayed(title: String, message: String, label: String = "") {
        onNodeWithText(title).assertIsDisplayed()
        onNodeWithText(message).assertIsDisplayed()
        if (label.isNotEmpty()){
            onNodeWithText(label).assertIsDisplayed()
        }
        onNodeWithText(appContext.getString(R.string.ok)).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.assertInitialScreenIsDisplayed(operationText: String) {
        // text
        onNodeWithText(title).assertIsDisplayed()
        onNodeWithText(screenTitle).assertIsDisplayed()
        onNodeWithText(operationText).assertIsDisplayed()
        // button
        onNodeWithText(startReadingButtonText).assertIsDisplayed()
        onNodeWithText(startReadingButtonText).assertIsNotEnabled()
    }

    private fun ComposeContentTestRule.assertScreenIsDoesNotExist(operationText: String) {
        // text
        onNodeWithText(title).assertDoesNotExist()
        onNodeWithText(screenTitle).assertDoesNotExist()
        onNodeWithText(operationText).assertDoesNotExist()
        // button
        onNodeWithText(startReadingButtonText).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.assertProgressIsDisplayed(message: String) {
        onNodeWithText(scanTitleReady).assertIsDisplayed()
        onNodeWithText(message).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.assertProgressIsDoesNotExist(message: String) {
        onNodeWithText(scanTitleReady).assertDoesNotExist()
        onNodeWithText(message).assertDoesNotExist()
    }

    @Test
    fun certReadScreenTest_userAuthentication() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            CertReadScreen(nfcAdapter, stateViewModel)
        }

        // 初期表示確認
        composeTestRule.assertInitialScreenIsDisplayed(operationTextUser)

        // textInput
        composeTestRule.onNodeWithText(labelPassword).performTextInput("1234")
        // button
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        composeTestRule.onNodeWithText(startReadingButtonText).performClick()

        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_ready))

        // 本来NFCカードタグがアタッチされた際にメッセージが変化するが、物理以外でタグをアタッチさせることが難しいため、カードがタッチされた際のメッセージの変化をテスト
        stateViewModel.updateProgressViewState(true, scanTitleReady, appContext.getString(R.string.scan_message_reading))

        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_reading))

        val keyCloakState = KeycloakState.Success("https://successfulUrl")
        stateViewModel.setState(keyCloakState, NfcState.Success)
        stateViewModel.updateProgressViewState(false)
        composeTestRule.assertScreenIsDoesNotExist(operationTextUser)
    }

    @Test
    fun certReadScreenTest_digitalSignatureAuthentication() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)

            CertReadScreen(nfcAdapter, stateViewModel)
        }

        // 初期表示確認
        composeTestRule.assertInitialScreenIsDisplayed(operationTextDigitalSignature)

        // text input
        composeTestRule.onNodeWithText(labelPassword).performTextInput("1234567890123456")
        // button
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        composeTestRule.onNodeWithText(startReadingButtonText).performClick()
        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_ready))

        // 本来NFCカードタグがアタッチされた際にメッセージが変化するが、物理以外でタグをアタッチさせることが難しいため、カードがタッチされた際のメッセージの変化をテスト
        stateViewModel.updateProgressViewState(true, scanTitleReady, appContext.getString(R.string.scan_message_reading))

        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_reading))
        val keyCloakState = KeycloakState.Success("https://successfulUrl")
        stateViewModel.setState(keyCloakState, NfcState.Success)
        stateViewModel.updateProgressViewState(false)
        composeTestRule.assertScreenIsDoesNotExist(operationTextDigitalSignature)
    }

    @Test
    fun certReadScreenTest_UnRegisterError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UnRegisterError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), appContext.getString(R.string.auth_failure_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_userAuthenticationLapseError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.LapseError, NfcState.Success)

        val message = appContext.getString(R.string.user_cert_lapse) + appContext.getString(R.string.lapse_description)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), message, appContext.getString(R.string.contact_page))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_authenticationLapseError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            stateViewModel.setUriParameters(createUriParameters("registration", "https://actionUrl", "https://error/test"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.LapseError, NfcState.Success)

        val message = appContext.getString(R.string.sign_cert_lapse) + appContext.getString(R.string.lapse_description)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), message, appContext.getString(R.string.contact_page))
        composeTestRule.onNodeWithText(appContext.getString(R.string.contact_page)).performClick()
    }

    @Test
    fun certReadScreenTest_InfoChangeError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        // 利用者証明用電子証明書読み取り画面表示
        composeTestRule.assertInitialScreenIsDisplayed(operationTextUser)

        stateViewModel.setState(KeycloakState.InfoChangeError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.my_number_card_reload), appContext.getString(R.string.my_number_card_reload_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()

        // 署名用電子証明書読み取り画面表示
        composeTestRule.assertInitialScreenIsDisplayed(operationTextDigitalSignature)
    }


    @Test
    fun certReadScreenTest_userDuplicateError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            stateViewModel.setUriParameters(createUriParameters("registration", "https://actionUrl", "https://error/test"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UserDuplicateError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.registration_failure), appContext.getString(R.string.registration_failure_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_IncorrectPin() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.Error, NfcState.IncorrectPin)
        stateViewModel.updateProgressViewState(false)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.failure), appContext.getString(R.string.in_correct_pin))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_TryCountIsNotLeft() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.Error, NfcState.TryCountIsNotLeft)
        stateViewModel.updateProgressViewState(false)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.failure), appContext.getString(R.string.try_count_Is_not_left))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_MyNumberCardReadFailure() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.Error, NfcState.Failure)
        stateViewModel.updateProgressViewState(false)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.failure), appContext.getString(R.string.my_number_card_read_failure))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_UnexpectedError() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://errorUrl"))
            stateViewModel.setExternalUrls(createExternalUls())
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.Error, NfcState.None)
        stateViewModel.updateProgressViewState(false)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.failure), appContext.getString(R.string.failure_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenReadingButtonTest_screenModeStateIsUserCertRead() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            CertReadScreen(nfcAdapter, stateViewModel)
        }

        composeTestRule.onNodeWithText(labelPassword).performTextInput("123")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsNotEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("4")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("5")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
    }

    @Test
    fun certReadScreenReadingButtonTest_screenModeStateIsSignCertRead() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            CertReadScreen(nfcAdapter, stateViewModel)
        }

        composeTestRule.onNodeWithText(labelPassword).performTextInput("12345")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsNotEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("6")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("7")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("8")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("9")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("0")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("1")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("2")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("3")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("4")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("5")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("6")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(labelPassword).performTextInput("7")
        composeTestRule.onNodeWithText(startReadingButtonText).assertIsEnabled()
    }

    @Test
    fun certReadScreenTest_nfcAdapterIsNull() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            CertReadScreen(null, stateViewModel)
        }
        composeTestRule.onNodeWithText(labelPassword).performTextInput("1234")
        composeTestRule.onNodeWithText(startReadingButtonText).performClick()
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_ready))
    }

    @Test
    fun showProgressTest_argumentIsOmitted() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.updateProgressViewState(true)
        composeTestRule.assertProgressIsDoesNotExist(appContext.getString(R.string.scan_message_ready))
    }

    @Test
    fun showProgressTest_argumentIsEmpty() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.updateProgressViewState(true, "", "")
        composeTestRule.assertProgressIsDoesNotExist(appContext.getString(R.string.scan_message_ready))
    }

    @Test
    fun certReadScreenTest_unRegisterError_urlReplaced() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl", "https://error/test&amp;page"))

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UnRegisterError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), appContext.getString(R.string.auth_failure_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_urlParametersIsNull() {
        try {
            composeTestRule.setContent {
                stateViewModel = viewModel(factory = StateViewModel.Factory)
                stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
                CertReadScreen(nfcAdapter, stateViewModel)
            }
            stateViewModel.setState(KeycloakState.UnRegisterError, NfcState.Success)
        } catch (e:NullPointerException) {
            composeTestRule.onNodeWithText(appContext.getString(R.string.auth_failure))
                .assertDoesNotExist()
            composeTestRule.onNodeWithText(appContext.getString(R.string.auth_failure_description))
                .assertDoesNotExist()
        }
    }

    @Test
    fun certReadScreenTest_unRegisterError_urlParametersErrorUrlIsNull() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(UriParameters("nonce", "login", "https://actionUrl", null))
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UnRegisterError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), appContext.getString(R.string.auth_failure_description))
    }

    @Test
    fun certReadScreenTest_unRegisterError_urlParametersErrorUrlIsEmpty() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)
            stateViewModel.setUriParameters(createUriParameters("login", "https://actionUrl"))
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UnRegisterError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.auth_failure), appContext.getString(R.string.auth_failure_description))
    }

    @Test
    fun certReadScreenTest_userDuplicateError_urlReplaced() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            stateViewModel.setUriParameters(createUriParameters("registration", "https://actionUrl", "https://error/test&amp;page"))
            stateViewModel.setExternalUrls(createExternalUls())

            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UserDuplicateError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.registration_failure), appContext.getString(R.string.registration_failure_description))
        composeTestRule.onNodeWithText(appContext.getString(R.string.ok)).performClick()
    }

    @Test
    fun certReadScreenTest_userDuplicateError_urlParametersErrorUrlIsNull() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            stateViewModel.setUriParameters(UriParameters("nonce", "registration", "https://actionUrl", null))
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UserDuplicateError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.registration_failure), appContext.getString(R.string.registration_failure_description))
    }

    @Test
    fun certReadScreenTest_userDuplicateError_urlParametersErrorUrlIsEmpty() {
        composeTestRule.setContent {
            stateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.SignCertRead)
            stateViewModel.setUriParameters(createUriParameters("registration", "https://actionUrl"))
            CertReadScreen(nfcAdapter, stateViewModel)
        }
        stateViewModel.setState(KeycloakState.UserDuplicateError, NfcState.Success)

        // errorDialog
        composeTestRule.assertAlertDialogIsDisplayed(appContext.getString(R.string.registration_failure), appContext.getString(R.string.registration_failure_description))
    }

    @Test
    fun certReadScreenTest_ErrorDialogDetail() {
        val title = "title"
        val message = "message"
        val url = "https://dummy/url\""
        val linqText = "https://dummy/contact/page"
        val screenMode: ScreenModeState = ScreenModeState.UserCertRead
        val errorDialogDetail = ErrorDialogDetail(
            title,
            message,
            url,
            linqText,
            screenMode
        )
        assert(errorDialogDetail.title == title)
        assert(errorDialogDetail.message == message)
        assert(errorDialogDetail.url == url)
        assert(errorDialogDetail.linqText == linqText)
        assert(errorDialogDetail.newScreenMode == screenMode)
    }

    @Test
    fun certReadScreenTest_ErrorDialogDetailArgContainsNull() {
        val title = "title"
        val message = "message"
        val errorDialogDetail = ErrorDialogDetail(
            title,
            message,
            null,
            null,
            null
        )
        assert(errorDialogDetail.title == title)
        assert(errorDialogDetail.message == message)
        assert(errorDialogDetail.url.isNullOrEmpty())
        assert(errorDialogDetail.linqText.isNullOrEmpty())
        assert(errorDialogDetail.newScreenMode == null)
    }
}
