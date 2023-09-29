package com.example.mynumbercardidp.ui.screen

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.ui.ExternalUrls
import com.example.mynumbercardidp.ui.viewmodel.StateViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualBootScreenKtTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    val appContext: Context = ApplicationProvider.getApplicationContext()
    val title = appContext.getString(R.string.title)
    val aboutThisApp = appContext.getString(R.string.about_this_app)
    val aboutThisAppDetail = appContext.getString(R.string.about_this_app_detail)
    val termsOfUrls = appContext.getString(R.string.terms_of_use)
    val privacyPolicy = appContext.getString(R.string.privacy_policy)
    val personInfoProtectionPolicy = appContext.getString(R.string.person_Info_protection_policy)

    @Test
    fun manualBootScreenTest_successfullyToDisplay() {
        val externalUrls = ExternalUrls(
            "dummy",
            "https://example.com/open-id/privacy-policy.html",
            "https://example.com/open-id/personal-data-protection-policy.html",
            "https://example.com/open-id/terms-of-use.html"
        )
        composeTestRule.setContent {
            var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
            stateViewModel.setExternalUrls(externalUrls)
            ManualBootScreen(stateViewModel)
        }
        // text
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(aboutThisApp).assertIsDisplayed()
        composeTestRule.onNodeWithText(aboutThisAppDetail).assertIsDisplayed()
        // button
        composeTestRule.onNodeWithText(termsOfUrls).assertIsDisplayed()
        composeTestRule.onNodeWithText(privacyPolicy).assertIsDisplayed()
        composeTestRule.onNodeWithText(personInfoProtectionPolicy).assertIsDisplayed()

        composeTestRule.onNodeWithText(termsOfUrls).performClick()
    }

    @Test(expected=NullPointerException::class)
    fun manualBootScreenTest_failToDisplay_whenArgumentIsNull01() {
        val externalUrls = ExternalUrls(
            "dummy",
            "https://example.com/open-id/privacy-policy.html",
            "https://example.com/open-id/personal-data-protection-policy.html",
            null
        )
        try {
            composeTestRule.setContent {
                var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
                stateViewModel.setExternalUrls(externalUrls)
                ManualBootScreen(stateViewModel)
            }
        } catch (e:NullPointerException) {
            composeTestRule.onNodeWithText(title).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisApp).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisAppDetail).assertDoesNotExist()

            composeTestRule.onNodeWithText(termsOfUrls).assertDoesNotExist()
            composeTestRule.onNodeWithText(privacyPolicy).assertDoesNotExist()
            composeTestRule.onNodeWithText(personInfoProtectionPolicy).assertDoesNotExist()

            throw e
        }
    }

    @Test(expected=NullPointerException::class)
    fun manualBootScreenTest_failToDisplay_whenArgumentIsNull02() {
        val externalUrls = ExternalUrls(
            "dummy",
            null,
            "https://example.com/open-id/personal-data-protection-policy.html",
            "https://example.com/open-id/terms-of-use.html"
        )
        try {
            composeTestRule.setContent {
                var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
                stateViewModel.setExternalUrls(externalUrls)
                ManualBootScreen(stateViewModel)
            }
        } catch (e:NullPointerException) {
            composeTestRule.onNodeWithText(title).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisApp).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisAppDetail).assertDoesNotExist()

            composeTestRule.onNodeWithText(termsOfUrls).assertDoesNotExist()
            composeTestRule.onNodeWithText(privacyPolicy).assertDoesNotExist()
            composeTestRule.onNodeWithText(personInfoProtectionPolicy).assertDoesNotExist()

            throw e
        }
    }

    @Test(expected=NullPointerException::class)
    fun manualBootScreenTest_failToDisplay_whenArgumentIsNull03() {
        val externalUrls = ExternalUrls(
            "dummy",
            "https://example.com/open-id/privacy-policy.html",
            null,
            "https://example.com/open-id/terms-of-use.html"
        )
        try {
            composeTestRule.setContent {
                var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
                stateViewModel.setExternalUrls(externalUrls)
                ManualBootScreen(stateViewModel)
            }
        } catch (e:NullPointerException) {
            composeTestRule.onNodeWithText(title).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisApp).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisAppDetail).assertDoesNotExist()

            composeTestRule.onNodeWithText(termsOfUrls).assertDoesNotExist()
            composeTestRule.onNodeWithText(privacyPolicy).assertDoesNotExist()
            composeTestRule.onNodeWithText(personInfoProtectionPolicy).assertDoesNotExist()

            throw e
        }
    }

    @Test(expected=NullPointerException::class)
    fun manualBootScreenTest_failToDisplay_whenExternalUrlsIsNull() {
        try {
            composeTestRule.setContent {
                var stateViewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
                ManualBootScreen(stateViewModel)
            }
        } catch (e:NullPointerException) {
            composeTestRule.onNodeWithText(title).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisApp).assertDoesNotExist()
            composeTestRule.onNodeWithText(aboutThisAppDetail).assertDoesNotExist()

            composeTestRule.onNodeWithText(termsOfUrls).assertDoesNotExist()
            composeTestRule.onNodeWithText(privacyPolicy).assertDoesNotExist()
            composeTestRule.onNodeWithText(personInfoProtectionPolicy).assertDoesNotExist()

            throw e
        }
    }
}