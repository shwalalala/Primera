package cit.edu.primera.feature.onboarding.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.feature.auth.ui.AuthTab
import cit.edu.primera.feature.auth.ui.AuthUiState
import cit.edu.primera.feature.auth.ui.RegisterScreen
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

class OnboardingFlowUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registerScreen_rendersAllFieldsAndTermsCheckbox() {
        composeTestRule.setContent {
            PrimeraTheme {
                RegisterScreen(
                    state = AuthUiState(
                        activeTab = AuthTab.SIGNUP,
                        username = "JaneDoe",
                        email = "janedoe@example.com"
                    ),
                    onUsernameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onAgreedToTermsToggle = {},
                    onTermsClicked = {},
                    onSignUpClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up to start your journey").assertIsDisplayed()
        composeTestRule.onNodeWithText("I agree to the ").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun birthdayStep_rendersTitleAndContinueButton() {
        composeTestRule.setContent {
            PrimeraTheme {
                BirthdayStep(
                    birthday = Calendar.getInstance().apply { add(Calendar.YEAR, -26) }.time,
                    onBirthdayChange = {},
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("When is your birthday?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun cycleRegularityStep_rendersCycleOptions() {
        composeTestRule.setContent {
            PrimeraTheme {
                CycleRegularityStep(
                    isCycleRegular = true,
                    onIsCycleRegularChange = {},
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Is your cycle regular?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, it's regular").assertIsDisplayed()
        composeTestRule.onNodeWithText("No, it varies").assertIsDisplayed()
    }

    @Test
    fun eddStep_rendersTentativeOrConfirmedEddTitle() {
        val eddDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 140) }.time
        composeTestRule.setContent {
            PrimeraTheme {
                EddStep(
                    isTentative = true,
                    positiveTestDate = null,
                    eddDate = eddDate,
                    onPositiveTestDateChange = {},
                    onEddDateChange = {},
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Tentative Due Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun firstPregnancyStep_rendersFirstVsPastPregnanciesOptions() {
        composeTestRule.setContent {
            PrimeraTheme {
                FirstPregnancyStep(
                    isFirstPregnancy = true,
                    onIsFirstPregnancyChange = {},
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Is this your first pregnancy?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, first pregnancy").assertIsDisplayed()
        composeTestRule.onNodeWithText("No, I've been pregnant before").assertIsDisplayed()
    }
}
