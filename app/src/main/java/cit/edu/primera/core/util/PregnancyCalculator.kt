package cit.edu.primera.core.util

import java.util.Calendar
import java.util.Date

/**
 * Single source of truth for calculating Estimated Due Date (EDD) and gestational age across
 * Profile, Onboarding, Dashboard, and Insights.
 */
object PregnancyCalculator {

    /**
     * Computes the estimated due date (EDD) based on available pregnancy indicators
     * in priority order: Explicit EDD > Ultrasound Scan > LMP > Positive Test Date.
     */
    fun calculateEdd(
        eddDate: Date? = null,
        scanDate: Date? = null,
        scanWeeks: Int? = null,
        scanDays: Int? = null,
        lmpDate: Date? = null,
        positiveTestDate: Date? = null
    ): Date? {
        if (eddDate != null) return eddDate

        if (scanDate != null && (scanWeeks != null || scanDays != null)) {
            val weeks = scanWeeks ?: 0
            val days = scanDays ?: 0
            val gestationalDaysAtScan = (weeks * 7) + days
            val daysRemaining = 280 - gestationalDaysAtScan
            return Calendar.getInstance().apply {
                time = scanDate
                add(Calendar.DAY_OF_YEAR, daysRemaining)
            }.time
        }

        if (lmpDate != null) {
            return Calendar.getInstance().apply {
                time = lmpDate
                add(Calendar.DAY_OF_YEAR, 280)
            }.time
        }

        if (positiveTestDate != null) {
            return Calendar.getInstance().apply {
                time = positiveTestDate
                add(Calendar.WEEK_OF_YEAR, 36)
            }.time
        }

        return null
    }

    /**
     * Computes total gestational days completed (0..294 days, i.e., 0..42 weeks).
     */
    fun getGestationalDays(dueDate: Date?): Int {
        if (dueDate == null) return 196
        val dueCal = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = dueCal.timeInMillis - currentCal.timeInMillis
        val calculatedDaysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val daysRemaining = calculatedDaysRemaining.coerceIn(-14, 280)
        return (280 - daysRemaining).coerceIn(0, 294)
    }

    fun getGestationalWeeks(dueDate: Date?): Int {
        return (getGestationalDays(dueDate) / 7).coerceIn(1, 42)
    }

    fun getGestationalDayOfCurrentWeek(dueDate: Date?): Int {
        return (getGestationalDays(dueDate) % 7).coerceIn(0, 6)
    }

    fun getDaysLeft(dueDate: Date?): Int {
        if (dueDate == null) return 84
        val dueCal = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = dueCal.timeInMillis - currentCal.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
