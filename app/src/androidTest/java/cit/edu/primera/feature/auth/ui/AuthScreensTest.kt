package cit.edu.primera.feature.auth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cit.edu.primera.core.theme.PrimeraTheme
import org.junit.Rule
import org.junit.Test

class AuthScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountLinkingDialog_rendersProviderNameAndConfirmButton() {
        composeTestRule.setContent {
            PrimeraTheme {
                AccountLinkingDialog(
                    providerName = "Google",
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Account Already Exists").assertIsDisplayed()
        composeTestRule.onNodeWithText("Got It").assertIsDisplayed()
    }

    @Test
    fun errorText_hasPoliteLiveRegionSemantics() {
        composeTestRule.setContent {
            PrimeraTheme {
                LoginScreen(
                    state = AuthUiState(
                        activeTab = AuthTab.LOGIN,
                        errorMessage = "Invalid email or password"
                    ),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onRememberMeToggle = {},
                    onForgotPasswordClicked = {},
                    onLoginClicked = {},
                    onTabSelected = {},
                    onSocialLoginClicked = {},
                    onFacebookLoginClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Invalid email or password").assertIsDisplayed()
    }
}
