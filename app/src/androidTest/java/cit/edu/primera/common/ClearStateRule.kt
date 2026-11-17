package cit.edu.primera.common

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.MainActivity
import com.google.firebase.auth.FirebaseAuth
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

/**
 * Ensures clean application state before and after each instrumented test.
 * Supports granular state resets, pm clear, and fresh ActivityScenario launches.
 */
class ClearStateRule(
    private val clearAuth: Boolean = false,
    private val clearPrefs: Boolean = false,
    private val clearDatabaseCache: Boolean = false,
    private val usePmClear: Boolean = false
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        try {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.executeShellCommand("pm grant cit.edu.primera android.permission.POST_NOTIFICATIONS")
        } catch (_: Exception) {}
        resetState()
    }

    override fun finished(description: Description?) {
        super.finished(description)
        resetState()
    }

    fun resetState() {
        if (usePmClear) {
            try {
                val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                device.executeShellCommand("pm clear cit.edu.primera")
            } catch (_: Exception) {}
            return
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        if (clearAuth) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}
        }

        if (clearPrefs) {
            try {
                val sharedPrefsDir = File(context.filesDir.parentFile, "shared_prefs")
                if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                    sharedPrefsDir.listFiles()?.forEach { file ->
                        val prefName = file.name.removeSuffix(".xml")
                        // Preserve Firebase Auth prefs if clearAuth is false
                        if (clearAuth || !prefName.startsWith("com.google.firebase.auth")) {
                            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                                .edit()
                                .clear()
                                .commit()
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        if (clearDatabaseCache) {
            try {
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    /**
     * Helper to launch a fresh ActivityScenario with clean state.
     */
    fun launchFreshScenario(): ActivityScenario<MainActivity> {
        resetState()
        return ActivityScenario.launch(MainActivity::class.java)
    }
}
