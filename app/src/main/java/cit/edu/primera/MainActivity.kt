package cit.edu.primera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cit.edu.primera.core.navigation.AppNavGraph
import cit.edu.primera.core.notification.NotificationHelper
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.core.theme.PrimeraTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private var pendingDestinationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestNotificationPermission()
        initializeReminders()

        handleNotificationIntent(intent)

        setContent {
            PrimeraTheme {
                AppNavGraph(
                    pendingDestinationRoute = pendingDestinationRoute,
                    onPendingRouteHandled = { pendingDestinationRoute = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val targetRoute = intent?.getStringExtra(NotificationHelper.EXTRA_DESTINATION_ROUTE)
        if (!targetRoute.isNullOrBlank()) {
            pendingDestinationRoute = targetRoute
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
