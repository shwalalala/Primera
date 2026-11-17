package cit.edu.primera.common

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.io.FileOutputStream

object ScreenshotHelper {

    private const val TAG = "ScreenshotHelper"

    fun getScreenshotDir(): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.getExternalFilesDir(null), "screenshots")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getSharedScreenshotDir(): File? {
        return try {
            val dir = File("/sdcard/screenshots")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            dir
        } catch (_: Exception) {
            null
        }
    }

    fun captureDevice(
        name: String,
        device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    ): File {
        val file = File(getScreenshotDir(), "$name.png")
        try {
            device.waitForIdle(1000)
            val success = device.takeScreenshot(file)
            Log.d(TAG, "Device screenshot saved: ${file.absolutePath}, success=$success")

            // Mirror to persistent /sdcard/screenshots so adb pull succeeds even if package is uninstalled
            getSharedScreenshotDir()?.let { sharedDir ->
                val sharedFile = File(sharedDir, "$name.png")
                try {
                    file.copyTo(sharedFile, overwrite = true)
                } catch (_: Exception) {
                    device.takeScreenshot(sharedFile)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture device screenshot $name", e)
        }
        return file
    }

    fun captureNode(name: String, node: SemanticsNodeInteraction): File {
        val file = File(getScreenshotDir(), "$name.png")
        try {
            val bitmap = node.captureToImage().asAndroidBitmap()
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Node screenshot saved: ${file.absolutePath}")

            getSharedScreenshotDir()?.let { sharedDir ->
                val sharedFile = File(sharedDir, "$name.png")
                try {
                    file.copyTo(sharedFile, overwrite = true)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture node screenshot $name", e)
        }
        return file
    }

    inline fun step(
        module: String,
        step: String,
        device: UiDevice,
        assertion: () -> Unit
    ) {
        val prefix = "M${module}_TC${step}"
        try {
            assertion()
            captureDevice("${prefix}_PASS", device)
        } catch (t: Throwable) {
            captureDevice("${prefix}_FAIL", device)
            throw t
        }
    }

    inline fun step(
        module: String,
        step: String,
        assertion: () -> Unit
    ) {
        step(module, step, UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()), assertion)
    }

    inline fun regressionStep(
        testNum: String,
        device: UiDevice,
        assertion: () -> Unit
    ) {
        val formattedNum = if (testNum.length < 2) testNum.padStart(2, '0') else testNum
        val prefix = "RT${formattedNum}"
        try {
            assertion()
            captureDevice("${prefix}_PASS", device)
        } catch (t: Throwable) {
            captureDevice("${prefix}_FAIL", device)
            throw t
        }
    }

    inline fun regressionStep(
        testNum: String,
        assertion: () -> Unit
    ) {
        regressionStep(testNum, UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()), assertion)
    }

    inline fun regressionStep(
        testNum: Int,
        device: UiDevice,
        assertion: () -> Unit
    ) {
        regressionStep(testNum.toString(), device, assertion)
    }

    inline fun regressionStep(
        testNum: Int,
        assertion: () -> Unit
    ) {
        regressionStep(testNum.toString(), UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()), assertion)
    }
}
