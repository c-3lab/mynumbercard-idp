package com.example.mynumbercardidp.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class UiStateTest {
    @Test
    fun testUiState_whenDefault() {
        var uiState = UiState()

        assertEquals(false, uiState.isNfcReading)
        assertEquals(KeycloakState.Loading, uiState.keycloakState)
        assertEquals(ScreenModeState.ManualBoot, uiState.screenMode)
        assertEquals(null, uiState.uriParameters)
        assertEquals(NfcState.None, uiState.nfcState)
        assertEquals(null, uiState.externalUrls)
        assertEquals("", uiState.nfcReadingTitle)
        assertEquals("", uiState.nfcReadingMessage)
    }

    @Test
    fun testUiState_whenInitialize() {
        var uriParameters = UriParameters()
        var externalUrls = ExternalUrls()
        var uiState = UiState(
            true, KeycloakState.Error, ScreenModeState.SignCertRead, uriParameters,
            NfcState.Failure, externalUrls,"title", "message"
        )

        assertEquals(true, uiState.isNfcReading)
        assertEquals(KeycloakState.Error, uiState.keycloakState)
        assertEquals(ScreenModeState.SignCertRead, uiState.screenMode)
        assertEquals(uriParameters, uiState.uriParameters)
        assertEquals(NfcState.Failure, uiState.nfcState)
        assertEquals(externalUrls, uiState.externalUrls)
        assertEquals("title", uiState.nfcReadingTitle)
        assertEquals("message", uiState.nfcReadingMessage)
    }

    @Test
    fun testUiState_whenUpdate() {
        var uriParameters = UriParameters()
        var externalUrls = ExternalUrls()
        var uiState = UiState()

        uiState.isNfcReading = true
        uiState.keycloakState = KeycloakState.Error
        uiState.screenMode = ScreenModeState.SignCertRead
        uiState.uriParameters = uriParameters
        uiState.nfcState = NfcState.Failure
        uiState.externalUrls = externalUrls
        uiState.nfcReadingTitle = "title"
        uiState.nfcReadingMessage = "message"

        assertEquals(true, uiState.isNfcReading)
        assertEquals(KeycloakState.Error, uiState.keycloakState)
        assertEquals(ScreenModeState.SignCertRead, uiState.screenMode)
        assertEquals(uriParameters, uiState.uriParameters)
        assertEquals(NfcState.Failure, uiState.nfcState)
        assertEquals(externalUrls, uiState.externalUrls)
        assertEquals("title", uiState.nfcReadingTitle)
        assertEquals("message", uiState.nfcReadingMessage)
    }


    @Test
    fun testUriParameters_whenDefault() {
        var uriParameters = UriParameters()

        assertEquals("", uriParameters.nonce)
        assertEquals("", uriParameters.mode)
        assertEquals("", uriParameters.action_url)
        assertEquals("", uriParameters.error_url)
    }

    @Test
    fun testUriParameters_whenInitialize() {
        var uriParameters = UriParameters(
            "nonce",
            "login",
            "https://example.com/action_url",
            "https://example.com/error_url"
        )

        assertEquals("nonce", uriParameters.nonce)
        assertEquals("login", uriParameters.mode)
        assertEquals("https://example.com/action_url", uriParameters.action_url)
        assertEquals("https://example.com/error_url", uriParameters.error_url)
    }

    @Test
    fun testUriParameters_whenUpdate() {
        var uriParameters = UriParameters()
        uriParameters.nonce = "nonce"
        uriParameters.mode = "login"
        uriParameters.action_url = "https://example.com/action_url"
        uriParameters.error_url = "https://example.com/error_url"

        assertEquals("nonce", uriParameters.nonce)
        assertEquals("login", uriParameters.mode)
        assertEquals("https://example.com/action_url", uriParameters.action_url)
        assertEquals("https://example.com/error_url", uriParameters.error_url)
    }

    @Test
    fun testExternalUrls_whenDefault() {
        var externalUrls = ExternalUrls()

        assertEquals("", externalUrls.inquiry)
        assertEquals("", externalUrls.privacyPolicy)
        assertEquals("", externalUrls.protectionPolicy)
        assertEquals("", externalUrls.termsOfUse)
    }

    @Test
    fun testExternalUrls_whenInitialize() {
        var externalUrls = ExternalUrls(
            "inquiry",
            "https://example.com/privacy_policy",
            "https://example.com/protection_policy",
            "https://example.com/term_of_use"
        )

        assertEquals("inquiry", externalUrls.inquiry)
        assertEquals("https://example.com/privacy_policy", externalUrls.privacyPolicy)
        assertEquals("https://example.com/protection_policy", externalUrls.protectionPolicy)
        assertEquals("https://example.com/term_of_use", externalUrls.termsOfUse)
    }

    @Test
    fun testExternalUrls_whenUpdate() {
        var externalUrls = ExternalUrls()
        externalUrls.inquiry = "inquiry"
        externalUrls.privacyPolicy = "https://example.com/privacy_policy"
        externalUrls.protectionPolicy = "https://example.com/protection_policy"
        externalUrls.termsOfUse = "https://example.com/term_of_use"

        assertEquals("inquiry", externalUrls.inquiry)
        assertEquals("https://example.com/privacy_policy", externalUrls.privacyPolicy)
        assertEquals("https://example.com/protection_policy", externalUrls.protectionPolicy)
        assertEquals("https://example.com/term_of_use", externalUrls.termsOfUse)
    }
}

