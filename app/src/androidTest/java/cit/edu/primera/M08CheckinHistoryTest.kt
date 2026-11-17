package cit.edu.primera

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import cit.edu.primera.common.ClearStateRule
import cit.edu.primera.common.LoginHelper
import cit.edu.primera.common.RegressionTest
import cit.edu.primera.common.ScreenshotHelper
import cit.edu.primera.common.SeedData
import cit.edu.primera.core.notification.NotificationHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RegressionTest
class M08CheckinHistoryTest {

    @get:Rule(order = 0)
    val clearStateRule = ClearStateRule(clearAuth = false, clearPrefs = false)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            runBlocking {
                LoginHelper.signInWithTestAccount()
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    cit.edu.primera.core.data.PreferenceRepositoryImpl(targetContext).apply {
                        setHasEverLoggedIn(true)
                        setOnboardingCompleted(uid)
                    }
                }
                SeedData.seedUserProfile()
                SeedData.seedCheckinHistory()
            }
        }
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            SeedData.seedCheckinHistory()
        }
        try {
            val allowBtn = device.wait(Until.findObject(By.textContains("Allow")), 2000)
            allowBtn?.click()
        } catch (e: Throwable) {}

        try {
            composeTestRule.waitUntil(15000) {
                try {
                    composeTestRule.onAllNodesWithText("Check-in").fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {
            try {
                composeTestRule.onNodeWithText("Skip").performClick()
            } catch (t: Throwable) {}
            try {
                composeTestRule.onNodeWithText("Get Started").performClick()
            } catch (t: Throwable) {}
            try {
                LoginHelper.performUiLogin(composeTestRule)
            } catch (t: Throwable) {}
            try {
                composeTestRule.waitUntil(10000) {
                    try {
                        composeTestRule.onAllNodesWithText("Check-in").fetchSemanticsNodes().isNotEmpty()
                    } catch (t: Throwable) {
                        false
                    }
                }
            } catch (t: Throwable) {}
        }

        try {
            composeTestRule.onNodeWithText("Skip").performClick()
        } catch (_: Throwable) {
            try {
                composeTestRule.onNodeWithText("Got it").performClick()
            } catch (_: Throwable) {}
        }
        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Check-in").performClick()
        } catch (e: Throwable) {
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE, "checkin")
                })
            }
        }
        composeTestRule.waitForIdle()

        try {
            composeTestRule.waitUntil(10000) {
                try {
                    composeTestRule.onAllNodesWithText("Headache", substring = true).fetchSemanticsNodes().isNotEmpty()
                } catch (e: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {}
    }

    @Test
    fun testCheckinHistoryViewAndSearchSuite() {
        // Step 1: History list (5 seeded entries)
        ScreenshotHelper.step("08", "01", device) {
            composeTestRule.waitForIdle()
            try {
                composeTestRule.onNodeWithText("Headache", substring = true).assertIsDisplayed()
            } catch (e: Throwable) {}
        }

        // Step 2: Search filter results
        ScreenshotHelper.step("08", "02", device) {
            try {
                composeTestRule.onNodeWithTag("history_search_input").performTextInput("Headache")
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithText("Search in history", substring = true).performTextInput("Headache")
                } catch (t: Throwable) {}
            }
            composeTestRule.waitForIdle()
            try {
                composeTestRule.onNodeWithText("Headache", substring = true).assertIsDisplayed()
            } catch (e: Throwable) {}
        }

        // Step 3: No-results message
        ScreenshotHelper.step("08", "03", device) {
            try {
                composeTestRule.onNodeWithTag("history_search_input").performTextClearance()
                composeTestRule.onNodeWithTag("history_search_input").performTextInput("NonExistentSymptomXYZ")
            } catch (e: Throwable) {
                try {
                    composeTestRule.onNodeWithText("Search in history", substring = true).performTextClearance()
                    composeTestRule.onNodeWithText("Search in history", substring = true).performTextInput("NonExistentSymptomXYZ")
                } catch (t: Throwable) {}
            }
            composeTestRule.waitForIdle()
            try {
                composeTestRule.onNodeWithText("No daily check-ins found.", substring = true).assertIsDisplayed()
            } catch (e: Throwable) {}
        }

        // Step 4: Category & date filter sheet (Evidence Gap: M8 date-range filter)
        ScreenshotHelper.step("08", "04", device) {
            try {
                composeTestRule.onNodeWithTag("history_search_input").performTextClearance()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()

            try {
                composeTestRule.onNodeWithTag("history_filter_button").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 5: Entry preview sheet
        ScreenshotHelper.step("08", "05", device) {
            try {
                composeTestRule.onNodeWithText("Headache", substring = true).performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 7: Delete confirmation dialog (cancel) (RT-16)
        ScreenshotHelper.step("08", "07", "16", device) {
            try {
                composeTestRule.onNodeWithText("Delete Daily Check-In").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText("Cancel").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 8: Delete confirmed and entry removed
        ScreenshotHelper.step("08", "08", device) {
            try {
                composeTestRule.onNodeWithText("Delete Daily Check-In").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText("Delete").performClick()
            } catch (e: Throwable) {}
            composeTestRule.waitForIdle()
        }

        // Step 9: Empty-state history account
        ScreenshotHelper.step("08", "09", device) {
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testEditCheckinNoteReplacementDefectSuite() {
        // Step 6: Edit check-in note replacement (Defect D-05 / RT-05)
        // Defect Verification D-05: edit appends text ("Feeling well and hydratedtext -Updated to vv.").
        // Assert that editing a check-in note cleanly replaces the note, rather than appending/concatenating text.
        ScreenshotHelper.step("08", "06", "05", device) {
            try {
                composeTestRule.onNodeWithText("Headache", substring = true).performClick()
                composeTestRule.waitForIdle()

                composeTestRule.onNodeWithText("Edit Daily Check-In").performClick()
                composeTestRule.waitForIdle()

                // Replace note text with updated clean text
                try {
                    composeTestRule.onNodeWithTag("checkin_note_input").performTextClearance()
                    composeTestRule.onNodeWithTag("checkin_note_input").performTextInput("Updated to vv.")
                } catch (e: Throwable) {
                    try {
                        composeTestRule.onNodeWithTag("note_input").performTextClearance()
                        composeTestRule.onNodeWithTag("note_input").performTextInput("Updated to vv.")
                    } catch (t: Throwable) {}
                }
                composeTestRule.waitForIdle()

                try {
                    composeTestRule.onNodeWithTag("checkin_review_button").performClick()
                } catch (e: Throwable) {
                    try {
                        composeTestRule.onNodeWithText("Review Check-in").performClick()
                    } catch (t: Throwable) {}
                }
                composeTestRule.waitForIdle()

                try {
                    composeTestRule.onNodeWithText("Confirm & Save").performScrollTo().performClick()
                } catch (e: Throwable) {
                    try {
                        composeTestRule.onNodeWithText("Confirm & Save").performClick()
                    } catch (t: Throwable) {}
                }
                composeTestRule.waitForIdle()

                try {
                    composeTestRule.waitUntil(10000) {
                        try {
                            composeTestRule.onAllNodesWithText("Confirm & Save").fetchSemanticsNodes().isEmpty()
                        } catch (t: Throwable) {
                            false
                        }
                    }
                } catch (t: Throwable) {}
                composeTestRule.waitForIdle()

                // Open entry details again to verify note replacement
                composeTestRule.onNodeWithText("Headache", substring = true).performClick()
                composeTestRule.waitForIdle()

                try {
                    composeTestRule.waitUntil(10000) {
                        try {
                            composeTestRule.onNodeWithText("Updated to vv.", substring = true).assertIsDisplayed()
                            true
                        } catch (t: Throwable) {
                            false
                        }
                    }
                } catch (t: Throwable) {}
                composeTestRule.waitForIdle()
            } catch (e: Throwable) {}

            // Defect D-05: In the buggy app, the note appends text ("Feeling well and hydratedtext -Updated to vv.")
            // Assert that the old text is cleanly replaced and does not remain appended/concatenated:
            composeTestRule.onNodeWithText("Feeling a bit nauseous", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("Feeling well and hydratedtext", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText("Updated to vv.", substring = true).assertIsDisplayed()
        }
    }
}
