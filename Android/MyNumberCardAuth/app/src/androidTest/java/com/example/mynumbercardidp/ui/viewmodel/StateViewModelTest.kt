package com.example.mynumbercardidp.ui.viewmodel

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.ui.ExternalUrls
import com.example.mynumbercardidp.ui.KeycloakState
import com.example.mynumbercardidp.ui.NfcState
import com.example.mynumbercardidp.ui.ScreenModeState
import com.example.mynumbercardidp.ui.UriParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StateViewModelTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNfcResultEquals() {
        var nfcResult = StateViewModel.NfcResult()
        assertTrue(nfcResult.equals(nfcResult))
    }

    @Test
    fun testNfcResultEquals_whenReturnFalse() {
        var nfcResult = StateViewModel.NfcResult()
        assertFalse(nfcResult.equals("dummy"))
    }

    @Test
    fun testNfcResultEquals_whenPassNull() {
        var nfcResult = StateViewModel.NfcResult()
        assertFalse(nfcResult.equals(null))
    }

    @Test
    fun testNfcResultEquals_whenSameRetDataReturnTrue() {
        var nfcResult01 = StateViewModel.NfcResult(null, byteArrayOf(1))
        var nfcResult02 = StateViewModel.NfcResult(null, byteArrayOf(1))
        assertTrue(nfcResult01.equals(nfcResult02))
    }

    @Test
    fun testNfcResultEquals_whenNotSameRetDataReturnFalse() {
        var nfcResult01 = StateViewModel.NfcResult(null, byteArrayOf(1))
        var nfcResult02 = StateViewModel.NfcResult(null, byteArrayOf(1, 1))
        assertFalse(nfcResult01.equals(nfcResult02))
    }

    @Test
    fun testHashCode() {
        var byteArray = byteArrayOf(1)
        var nfcResult = StateViewModel.NfcResult(null, byteArray)
        assertEquals(byteArray.contentHashCode(), nfcResult.hashCode())
    }

    @Test
    fun testUpdateProgressViewState() {
        composeTestRule.setContent {
            var activity = LocalContext.current as Activity
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.updateProgressViewState(true, activity.getString(R.string.scan_title_ready), activity.getString(R.string.scan_message_reading))

            assertEquals(true, stateViewModel.uiState.value.isNfcReading)
            assertEquals(activity.getString(R.string.scan_title_ready), stateViewModel.uiState.value.nfcReadingTitle)
            assertEquals(activity.getString(R.string.scan_message_reading), stateViewModel.uiState.value.nfcReadingMessage)
        }
    }

    @Test
    fun testSetState() {
        composeTestRule.setContent {
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.setState(KeycloakState.Success("https://successfulUrl"), NfcState.Success)

            assertEquals(KeycloakState.Success("https://successfulUrl"), stateViewModel.uiState.value.keycloakState)
            assertEquals(NfcState.Success, stateViewModel.uiState.value.nfcState)
        }
    }

    @Test
    fun testChangeViewMode() {
        composeTestRule.setContent {
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.changeViewMode(ScreenModeState.UserCertRead)

            assertEquals(ScreenModeState.UserCertRead, stateViewModel.uiState.value.screenMode)
        }
    }

    @Test
    fun testSetUriParameters() {
        val uriParameters = UriParameters(
            "nonce",
            "login",
            "https://example.com/action-url",
            "https://example.com/error-url"
        )

        composeTestRule.setContent {
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.setUriParameters(uriParameters)

            assertEquals("nonce", stateViewModel.uiState.value.uriParameters?.nonce)
            assertEquals("login", stateViewModel.uiState.value.uriParameters?.mode)
            assertEquals("https://example.com/action-url", stateViewModel.uiState.value.uriParameters?.action_url)
            assertEquals("https://example.com/error-url", stateViewModel.uiState.value.uriParameters?.error_url)
        }
    }

    @Test
    fun testSetExternalUrls() {
        val externalUrls = ExternalUrls(
            "dummy",
            "https://example.com/open-id/privacy-policy.html",
            "https://example.com/open-id/personal-data-protection-policy.html",
            "https://example.com/open-id/terms-of-use.html"
        )

        composeTestRule.setContent {
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.setExternalUrls(externalUrls)

            assertEquals("dummy", stateViewModel.uiState.value.externalUrls?.inquiry)
            assertEquals("https://example.com/open-id/privacy-policy.html", stateViewModel.uiState.value.externalUrls?.privacyPolicy)
            assertEquals("https://example.com/open-id/personal-data-protection-policy.html", stateViewModel.uiState.value.externalUrls?.protectionPolicy)
            assertEquals("https://example.com/open-id/terms-of-use.html", stateViewModel.uiState.value.externalUrls?.termsOfUse)
        }
    }
}