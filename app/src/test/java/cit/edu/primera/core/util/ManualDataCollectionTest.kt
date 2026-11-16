package cit.edu.primera.core.util

import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualDataCollectionTest {

    @Test
    fun manualEntry_fieldsAndSource_arePreserved() {
        val manualRecord = SmartwatchHealth(
            date = "2024-10-24",
            time = "09:45 AM",
            steps = 8500,
            currentHeartRate = 78,
            averageHeartRate = 78,
            spO2 = 98.5,
            sleepMinutes = 480,
            syncedAt = 1729758600000L,
            isManualEntry = true,
            source = "manual"
        )

        assertTrue(manualRecord.isManualEntry)
        assertEquals("manual", manualRecord.source)
        assertEquals("09:45 AM", manualRecord.time)
        assertEquals(8500L, manualRecord.steps)
        assertEquals(78L, manualRecord.currentHeartRate)
    }

    @Test
    fun generateSummaryText_includesManualEntryData_forQrCode() {
        val manualRecord = SmartwatchHealth(
            date = "2024-10-24",
            time = "09:45 AM",
            steps = 9200,
            currentHeartRate = 74,
            averageHeartRate = 74,
            spO2 = 99.0,
            sleepMinutes = 450,
            syncedAt = 1729758600000L,
            isManualEntry = true,
            source = "manual"
        )

        val patientData = DashboardData(
            email = "patient@example.com",
            firstName = "Jane",
            lastName = "Doe",
            userName = "Jane Doe",
            dueDate = null,
            steps = 9200,
            stepsGoal = 8000,
            heartRateBpm = 74,
            sleepHours = 7,
            sleepMinutes = 30,
            spO2 = 99,
            recentLogs = emptyList()
        )

        val qrSummaryText = DataExportHelper.generateSummaryText(
            healthRecords = listOf(manualRecord),
            dashboardData = patientData
        )

        assertTrue(qrSummaryText.contains("Patient: Jane Doe"))
        assertTrue(qrSummaryText.contains("2024-10-24"))
        assertTrue(qrSummaryText.contains("09:45 AM"))
        assertTrue(qrSummaryText.contains("[Self-Reported Manual]"))
        assertTrue(qrSummaryText.contains("Steps: 9200"))
        assertTrue(qrSummaryText.contains("74 bpm"))
    }
}
