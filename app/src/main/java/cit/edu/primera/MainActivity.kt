
package cit.edu.primera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cit.edu.primera.core.navigation.AppNavGraph
import cit.edu.primera.core.notification.AppointmentReminderScheduler
import cit.edu.primera.core.notification.NotificationHelper
import cit.edu.primera.core.notification.PregnancyMilestoneNotifier
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.feature.checkins.data.PrenatalDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingDestinationRoute by
    mutableStateOf<String?>(null)

    // ---------------------------------------------
    // PREGNANCY MILESTONE NOTIFICATIONS
    // ---------------------------------------------

    private val milestoneNotifier by lazy {
        PregnancyMilestoneNotifier(applicationContext)
    }

    private var milestoneCheckJob: Job? = null

    // ---------------------------------------------
    // APPOINTMENT REMINDER SYNCHRONIZATION
    // ---------------------------------------------

    private val appointmentScheduler by lazy {
        AppointmentReminderScheduler(applicationContext)
    }

    private var appointmentSyncJob: Job? = null

    // ---------------------------------------------
    // FIREBASE AUTH LISTENER
    // ---------------------------------------------

    private val authStateListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->

            if (firebaseAuth.currentUser != null) {
                checkPregnancyMilestone()
            }

            // Appointment synchronization observes
            // authentication changes automatically
            // through PrenatalDataSource.
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Preserve existing notification permissions.
        requestNotificationPermission()

        // Preserve existing daily reminders.
        initializeReminders()

        // Preserve notification navigation.
        handleNotificationIntent(intent)

        setContent {
            PrimeraTheme {
                AppNavGraph(
                    pendingDestinationRoute =
                        pendingDestinationRoute,
                    onPendingRouteHandled = {
                        pendingDestinationRoute = null
                    }
                )
            }
        }
    }

    // ---------------------------------------------
    // APP LIFECYCLE
    // ---------------------------------------------

    override fun onStart() {
        super.onStart()

        FirebaseAuth.getInstance()
            .addAuthStateListener(authStateListener)

        // Start listening for scheduled appointments.
        startAppointmentSynchronization()
    }

    override fun onResume() {
        super.onResume()

        // Preserve existing pregnancy milestone checks.
        checkPregnancyMilestone()
    }

    override fun onStop() {
        FirebaseAuth.getInstance()
            .removeAuthStateListener(authStateListener)

        // Stop observing while the activity is stopped.
        // Previously scheduled alarms remain active.
        appointmentSyncJob?.cancel()
        appointmentSyncJob = null

        super.onStop()
    }

    // ---------------------------------------------
    // NOTIFICATION NAVIGATION
    // ---------------------------------------------

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(
        intent: Intent?
    ) {
        val targetRoute =
            intent?.getStringExtra(
                NotificationHelper.EXTRA_DESTINATION_ROUTE
            )

        if (!targetRoute.isNullOrBlank()) {
            pendingDestinationRoute = targetRoute
        }
    }

    // ---------------------------------------------
    // NOTIFICATION PERMISSION
    // ---------------------------------------------

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    101
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {
            checkPregnancyMilestone()
        }
    }

    // ---------------------------------------------
    // EXISTING DAILY REMINDERS
    // ---------------------------------------------

    private fun initializeReminders() {
        ReminderManager(this)
            .scheduleDailyReminders()
    }

    // ---------------------------------------------
    // AUTOMATIC PREGNANCY MILESTONE CHECK
    // ---------------------------------------------

    private fun checkPregnancyMilestone() {
        if (
            FirebaseAuth.getInstance().currentUser == null
        ) {
            return
        }

        // Avoid overlapping Firestore checks.
        if (milestoneCheckJob?.isActive == true) {
            return
        }

        milestoneCheckJob = lifecycleScope.launch {
            milestoneNotifier.checkAndNotify()
        }
    }

    // ---------------------------------------------
    // AUTOMATIC APPOINTMENT REMINDER SYNC
    // ---------------------------------------------

    private fun startAppointmentSynchronization() {

        // Prevent duplicate Firestore observers.
        if (appointmentSyncJob?.isActive == true) {
            return
        }

        appointmentSyncJob = lifecycleScope.launch {

            PrenatalDataSource()
                .observeRecords()
                .catch { error ->
                    // Preserve already scheduled alarms
                    // if Firestore is temporarily unavailable.
                    Log.e(
                        "AppointmentReminders",
                        "Unable to observe prenatal appointments",
                        error
                    )
                }
                .collect { records ->

                    val userId =
                        FirebaseAuth.getInstance()
                            .currentUser?.uid

                    if (!userId.isNullOrBlank()) {
                        appointmentScheduler.synchronize(
                            userId = userId,
                            records = records
                        )
                    }
                }
        }
    }
}
