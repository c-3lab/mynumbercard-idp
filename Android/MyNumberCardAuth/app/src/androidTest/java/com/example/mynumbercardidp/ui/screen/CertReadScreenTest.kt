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
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CertReadScreenTest {
    var nfcAdapter: NfcAdapter? = null
    val appContext: Context = ApplicationProvider.getApplicationContext()
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
        onNodeWithText(screenTitle).assertIsDisplayed()
        onNodeWithText(operationText).assertIsDisplayed()
        // button
        onNodeWithText(startReadingButtonText).assertIsDisplayed()
        onNodeWithText(startReadingButtonText).assertIsNotEnabled()
    }

    private fun ComposeContentTestRule.assertProgressIsDisplayed(message: String) {
        onNodeWithText(scanTitleReady).assertIsDisplayed()
        onNodeWithText(message).assertIsDisplayed()
    }

    fun certReadScreenTest_userAuthentication() {
        composeTestRule.setContent {
            nfcAdapter = NfcAdapter.getDefaultAdapter(appContext)
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

        // TODO:本来NFCカードタグがアタッチされた際にメッセージが変化するが、物理以外でタグをアタッチさせることが難しいため、カードがタッチされた際のメッセージの変化をテスト
        stateViewModel.updateProgressViewState(true, scanTitleReady, appContext.getString(R.string.scan_message_reading))

        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_reading))

        val keyCloakState = KeycloakState.Success("https://successfulUrl")
        stateViewModel.setState(keyCloakState, NfcState.Success)
        stateViewModel.updateProgressViewState(false)
    }

    @Test
    fun certReadScreenTest_digitalSignatureAuthentication() {
        composeTestRule.setContent {
            nfcAdapter = NfcAdapter.getDefaultAdapter(appContext)
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

        // TODO:本来NFCカードタグがアタッチされた際にメッセージが変化するが、物理以外でタグをアタッチさせることが難しいため、カードがタッチされた際のメッセージの変化をテスト
        stateViewModel.updateProgressViewState(true, scanTitleReady, appContext.getString(R.string.scan_message_reading))

        // progress
        composeTestRule.assertProgressIsDisplayed(appContext.getString(R.string.scan_message_reading))

        val keyCloakState = KeycloakState.Success("https://successfulUrl")
        stateViewModel.setState(keyCloakState, NfcState.Success)
        stateViewModel.updateProgressViewState(false)
    }

    @Test
    fun certReadScreenReadingButtonTest_screenModeStateIsUserCertRead() {
        composeTestRule.setContent {
            nfcAdapter = NfcAdapter.getDefaultAdapter(appContext)
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
            nfcAdapter = NfcAdapter.getDefaultAdapter(appContext)
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
}
