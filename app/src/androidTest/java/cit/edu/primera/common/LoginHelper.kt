package cit.edu.primera.common

import android.util.Log
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object LoginHelper {

    suspend fun signInWithTestAccount(
        email: String = SeedData.TEST_USER_EMAIL,
        pass: String = SeedData.TEST_USER_PASS
    ) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d("LoginHelper", "User already signed in: ${auth.currentUser?.uid}")
            return
        }

        try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Log.d("LoginHelper", "Signed in with primary test account: ${auth.currentUser?.uid}")
            return
        } catch (e: Exception) {
            Log.w("LoginHelper", "signInWithEmailAndPassword failed: ${e.message}")
        }

        // If primary sign-in failed (e.g. rate-limited), try creating a unique test user
        val fallbackEmail = "primera.qa.${System.currentTimeMillis()}@example.com"
        try {
            auth.createUserWithEmailAndPassword(fallbackEmail, pass).await()
            Log.d("LoginHelper", "Created fresh test account: ${auth.currentUser?.uid} ($fallbackEmail)")
            return
        } catch (e2: Exception) {
            Log.w("LoginHelper", "createUserWithEmailAndPassword fallback failed: ${e2.message}")
        }

        // Try anonymous sign-in as last resort
        try {
            auth.signInAnonymously().await()
            Log.d("LoginHelper", "Signed in anonymously: ${auth.currentUser?.uid}")
        } catch (e3: Exception) {
            Log.e("LoginHelper", "All authentication methods failed", e3)
        }
    }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
    }

    fun performUiLogin(
        composeTestRule: ComposeContentTestRule,
        email: String = SeedData.TEST_USER_EMAIL,
        pass: String = SeedData.TEST_USER_PASS
    ) {
        try {
            composeTestRule.onNodeWithTag("tab_login").performClick()
        } catch (_: Exception) {
            try {
                composeTestRule.onNodeWithText("Log In").performClick()
            } catch (_: Exception) {}
        }

        try {
            composeTestRule.onNodeWithTag("login_email_input").performTextInput(email)
        } catch (_: Exception) {
            try {
                composeTestRule.onNodeWithText("Email").performTextInput(email)
            } catch (_: Exception) {}
        }

        try {
            composeTestRule.onNodeWithTag("login_password_input").performTextInput(pass)
        } catch (_: Exception) {
            try {
                composeTestRule.onNodeWithText("Password").performTextInput(pass)
            } catch (_: Exception) {}
        }

        try {
            composeTestRule.onNodeWithTag("login_submit_button").performClick()
        } catch (_: Exception) {
            try {
                composeTestRule.onNodeWithText("Log In").performClick()
            } catch (_: Exception) {}
        }
    }

    fun driveGoogleAccountPicker(device: UiDevice, timeoutMs: Long = 5000): Boolean {
        val accountChooser = device.wait(
            Until.findObject(By.textContains("@")),
            timeoutMs
        )
        if (accountChooser != null) {
            accountChooser.click()
            device.waitForIdle(1000)
            return true
        }

        val continueBtn = device.wait(
            Until.findObject(By.textContains("Continue as")),
            1000
        )
        if (continueBtn != null) {
            continueBtn.click()
            device.waitForIdle(1000)
            return true
        }

        return false
    }
}
