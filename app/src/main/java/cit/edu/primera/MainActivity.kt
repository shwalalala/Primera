package cit.edu.primera

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.navigation.AppNavGraph
import cit.edu.primera.core.notification.ReminderManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestNotificationPermission()
        initializeReminders()

        setContent {
            PrimeraTheme {
                AppNavGraph()
//                SmartwatchRoute()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101,
                )
            }
        }
    }

    private fun initializeReminders() {
        ReminderManager(this).scheduleDailyReminders()
    }
}
