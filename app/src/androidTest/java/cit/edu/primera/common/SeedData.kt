package cit.edu.primera.common

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.units.Percentage
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cit.edu.primera.core.util.CryptoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SeedData {

    const val TEST_USER_EMAIL = "primera.tester+reg@example.com"
    const val TEST_USER_PASS = "TestPass123!"
    const val TEST_USER_NAME = "SSTestUser"

    // Health Connect adb / toolbox command templates
    const val ADB_CMD_HEALTH_CONNECT_TOOLBOX = "am start -n com.google.android.apps.healthdata.toolbox/.MainActivity"
    const val ADB_CMD_SEED_STEPS = "cmd healthconnect insert steps --count 8420"
    const val ADB_CMD_SEED_HEART_RATE = "cmd healthconnect insert heart_rate --bpm 78"
    const val ADB_CMD_SEED_SLEEP = "cmd healthconnect insert sleep --duration 450"
    const val ADB_CMD_SEED_SPO2 = "cmd healthconnect insert spo2 --percentage 98"

    /**
     * Connects FirebaseAuth and FirebaseFirestore clients to local Firebase emulators.
     */
    fun connectToEmulators(
        host: String = "10.0.2.2",
        authPort: Int = 9099,
        firestorePort: Int = 8080
    ) {
        try {
            FirebaseAuth.getInstance().useEmulator(host, authPort)
        } catch (_: Exception) {}
        try {
            FirebaseFirestore.getInstance().useEmulator(host, firestorePort)
        } catch (_: Exception) {}
    }

    /**
     * Seeds Health Connect test data via HealthConnectClient.
     * Records inserted: Steps (8,420), Heart Rate (78 bpm avg), Sleep (7h 30m), SpO2 (98%).
     */
    suspend fun seedHealthConnectData(context: Context = InstrumentationRegistry.getInstrumentation().targetContext) {
        try {
            val client = HealthConnectClient.getOrCreate(context)
            val now = Instant.now()
            val zoneOffset = ZonedDateTime.now().offset

            val records = mutableListOf<androidx.health.connect.client.records.Record>()

            val defaultMetadata = androidx.health.connect.client.records.metadata.Metadata.manualEntry()

            // 1. Steps record: 8,420 steps today
            val stepsStartTime = now.minus(4, ChronoUnit.HOURS)
            records.add(
                StepsRecord(
                    startTime = stepsStartTime,
                    startZoneOffset = zoneOffset,
                    endTime = now.minus(10, ChronoUnit.MINUTES),
                    endZoneOffset = zoneOffset,
                    count = 8420L,
                    metadata = defaultMetadata
                )
            )

            // 2. Heart rate record: 78 bpm avg
            val hrStartTime = now.minus(2, ChronoUnit.HOURS)
            val samples = listOf(
                HeartRateRecord.Sample(time = hrStartTime, beatsPerMinute = 74L),
                HeartRateRecord.Sample(time = hrStartTime.plus(30, ChronoUnit.MINUTES), beatsPerMinute = 80L),
                HeartRateRecord.Sample(time = now.minus(15, ChronoUnit.MINUTES), beatsPerMinute = 78L)
            )
            records.add(
                HeartRateRecord(
                    startTime = hrStartTime,
                    startZoneOffset = zoneOffset,
                    endTime = now.minus(15, ChronoUnit.MINUTES),
                    endZoneOffset = zoneOffset,
                    samples = samples,
                    metadata = defaultMetadata
                )
            )

            // 3. Sleep session: 7h 30m last night
            val sleepStart = now.minus(16, ChronoUnit.HOURS)
            val sleepEnd = now.minus(8, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES)
            records.add(
                SleepSessionRecord(
                    startTime = sleepStart,
                    startZoneOffset = zoneOffset,
                    endTime = sleepEnd,
                    endZoneOffset = zoneOffset,
                    title = "Night Sleep",
                    metadata = defaultMetadata
                )
            )

            // 4. SpO2: 98%
            records.add(
                OxygenSaturationRecord(
                    time = now.minus(1, ChronoUnit.HOURS),
                    zoneOffset = zoneOffset,
                    percentage = Percentage(98.0),
                    metadata = defaultMetadata
                )
            )

            client.insertRecords(records)
        } catch (_: Exception) {
            // In emulator environments where Health Connect backend is mock/stubbed, ignore failure
        }
    }

    /**
     * Seeds Health Connect test data via ADB shell / broadcast or toolbox if installed.
     */
    fun seedHealthConnectViaShell(
        device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    ) {
        try {
            device.executeShellCommand("am broadcast -a androidx.health.connect.action.INSERT_RECORD --es type Steps --ei count 8420")
            device.executeShellCommand("am broadcast -a androidx.health.connect.action.INSERT_RECORD --es type HeartRate --ei bpm 78")
            device.executeShellCommand("am broadcast -a androidx.health.connect.action.INSERT_RECORD --es type Sleep --ei duration 450")
            device.executeShellCommand("am broadcast -a androidx.health.connect.action.INSERT_RECORD --es type SpO2 --ei percentage 98")
        } catch (_: Exception) {}
    }

    suspend fun seedUserProfile(
        userId: String? = null,
        username: String = TEST_USER_NAME,
        lmpDaysAgo: Int = 0 // 0 days ago gives Week 1, Day 0
    ) {
        val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -lmpDaysAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lmpDate = cal.time
        val dueDate = Calendar.getInstance().apply {
            time = lmpDate
            add(Calendar.DAY_OF_YEAR, 280)
        }.time

        val userDoc = mapOf(
            "role" to "patient",
            "username" to username,
            "email" to (FirebaseAuth.getInstance().currentUser?.email ?: TEST_USER_EMAIL),
            "lmpDate" to CryptoManager.encryptDate(lmpDate),
            "eddDate" to CryptoManager.encryptDate(dueDate),
            "steps" to 8420L,
            "stepsGoal" to 8000L,
            "heartRateBpm" to 78L,
            "sleepHours" to 7L,
            "sleepMinutes" to 30L,
            "spO2" to 98L,
            "weightKg" to CryptoManager.encryptLong(60L),
            "heightCm" to CryptoManager.encryptLong(165L),
            "isCycleRegular" to CryptoManager.encryptBoolean(true),
            "hasHadUltrasound" to CryptoManager.encryptBoolean(true),
            "onboardingCompleted" to true
        )

        try {
            cit.edu.primera.core.data.PreferenceRepositoryImpl(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).apply {
                setHasEverLoggedIn(true)
                setOnboardingCompleted(uid)
            }
        } catch (_: Exception) {}

        try {
            firestore.collection("users").document(uid)
                .set(userDoc, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("SeedData", "Failed to seed user profile", e)
        }
    }

    /**
     * Seeds user profile reproducing Defect D-07 where LMP is Aug 21, 2026,
     * but EDD is set to Aug 20, 2027 (and gestational age shows "1 week, 0 days").
     */
    suspend fun seedUserProfileWithLmpAug21(
        userId: String? = null,
        username: String = TEST_USER_NAME
    ) {
        val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val calLmp = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 21, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calBuggyDue = Calendar.getInstance().apply {
            set(2027, Calendar.AUGUST, 20, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val userDoc = mapOf(
            "role" to "patient",
            "username" to username,
            "email" to (FirebaseAuth.getInstance().currentUser?.email ?: TEST_USER_EMAIL),
            "lmpDate" to CryptoManager.encryptDate(calLmp.time),
            "eddDate" to CryptoManager.encryptDate(calBuggyDue.time),
            "steps" to 8420L,
            "stepsGoal" to 8000L,
            "heartRateBpm" to 78L,
            "sleepHours" to 7L,
            "sleepMinutes" to 30L,
            "spO2" to 98L,
            "weightKg" to CryptoManager.encryptLong(60L),
            "heightCm" to CryptoManager.encryptLong(165L),
            "isCycleRegular" to CryptoManager.encryptBoolean(true),
            "hasHadUltrasound" to CryptoManager.encryptBoolean(true),
            "onboardingCompleted" to true
        )

        try {
            cit.edu.primera.core.data.PreferenceRepositoryImpl(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).apply {
                setHasEverLoggedIn(true)
                setOnboardingCompleted(uid)
            }
        } catch (_: Exception) {}

        try {
            firestore.collection("users").document(uid)
                .set(userDoc, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("SeedData", "Failed to seed user profile with LMP Aug 21", e)
        }
    }

    suspend fun seedCheckinHistory(userId: String? = null) {
        val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        val sampleLogs = listOf(
            Triple(0, "Headache; Nausea", "Feeling a bit nauseous with a mild headache."),
            Triple(1, "Fatigue", "Tired after work, rested early."),
            Triple(2, "Happy", "Feeling energetic and hydrated today."),
            Triple(3, "Back Pain", "Mild lower back pain after walking."),
            Triple(4, "Normal", "Routine day, all vitals steady.")
        )

        for ((daysAgo, category, notes) in sampleLogs) {
            val logCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val dateStr = sdf.format(logCal.time)
            val logDoc = mapOf(
                "userId" to uid,
                "type" to "Check-in",
                "category" to category,
                "message" to notes,
                "description" to "Symptoms: $category; Weight: 60 kg; $notes",
                "timestamp" to logCal.time,
                "checkinDate" to dateStr
            )
            try {
                firestore.collection("checkins").add(logDoc).await()
            } catch (e: Exception) {
                android.util.Log.e("SeedData", "Failed to seed checkin", e)
            }
        }
    }
}
