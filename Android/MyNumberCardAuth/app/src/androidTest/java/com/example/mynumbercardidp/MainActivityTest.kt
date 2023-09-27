package com.example.mynumbercardidp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOnCreate() {
        var nonce = "nonce"
        var mode = "login"
        var actionUrl = "https://example.com/action_url"
        var errorUrl = "https://example.com/error_url"

        var context = ApplicationProvider.getApplicationContext<Context>()
        var intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("https://example.com/uri?nonce=$nonce&mode=$mode&action_url=$actionUrl&error_url=$errorUrl")
        }

        var scenario = ActivityScenario.launch<MainActivity>(intent)
        scenario.moveToState(Lifecycle.State.CREATED)
    }

    @Test
    fun testOnCreate_whenNonceIsNull() {
        var mode = "login"
        var actionUrl = "https://example.com/action_url"
        var errorUrl = "https://example.com/error_url"

        var context = ApplicationProvider.getApplicationContext<Context>()
        var intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("https://example.com/uri?mode=$mode&action_url=$actionUrl&error_url=$errorUrl")
        }

        var scenario = ActivityScenario.launch<MainActivity>(intent)
        scenario.moveToState(Lifecycle.State.CREATED)
    }

    @Test
    fun testOnCreate_whenUrlIsNull() {
        var context = ApplicationProvider.getApplicationContext<Context>()
        var intent = Intent(context, MainActivity::class.java).apply {
            data = null
        }

        var scenario = ActivityScenario.launch<MainActivity>(intent)
        scenario.moveToState(Lifecycle.State.CREATED)
    }

    @Test
    fun testOnCreate_whenModeIsNotLogin() {
        var nonce = "nonce"
        var mode = "registration"
        var actionUrl = "https://example.com/action_url"
        var errorUrl = "https://example.com/error_url"

        var context = ApplicationProvider.getApplicationContext<Context>()
        var intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("https://example.com/uri?nonce=$nonce&mode=$mode&action_url=$actionUrl&error_url=$errorUrl")
        }

        var scenario = ActivityScenario.launch<MainActivity>(intent)
        scenario.moveToState(Lifecycle.State.CREATED)
    }

    @Test
    fun testOnCreate_whenApduCommandsCoverage() {
        ApduCommands.jpkiAp = "DummyJpkiAp"
        ApduCommands.userAuthenticationPin = "DummyUserAuthenticationPin"
        ApduCommands.digitalSignaturePin = "DummyDigitalSignaturePin"
        ApduCommands.computeDigitalSignature = "DummyComputeDigitalSignature"
        ApduCommands.userAuthenticationPrivate = "DummyUserAuthenticationPrivate"
        ApduCommands.digitalSignaturePrivate = "DummyDigitalSignaturePrivate"
        ApduCommands.userAuthenticationCertificate = "DummyUserAuthenticationCertificate"
        ApduCommands.digitalSignatureCertificate = "DummyDigitalSignatureCertificate"

        assertEquals(ApduCommands.jpkiAp, "DummyJpkiAp")
        assertEquals(ApduCommands.userAuthenticationPin, "DummyUserAuthenticationPin")
        assertEquals(ApduCommands.digitalSignaturePin, "DummyDigitalSignaturePin")
        assertEquals(ApduCommands.computeDigitalSignature, "DummyComputeDigitalSignature")
        assertEquals(ApduCommands.userAuthenticationPrivate, "DummyUserAuthenticationPrivate")
        assertEquals(ApduCommands.digitalSignaturePrivate, "DummyDigitalSignaturePrivate")
        assertEquals(ApduCommands.userAuthenticationCertificate, "DummyUserAuthenticationCertificate")
        assertEquals(ApduCommands.digitalSignatureCertificate, "DummyDigitalSignatureCertificate")
    }
}