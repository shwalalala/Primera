package cit.edu.primera.feature.dashboard.domain

import java.util.Date

data class DashboardData(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val userName: String,
    val birthday: Date? = null,
    val lmpDate: Date? = null,
    val dueDate: Date?,
    val steps: Int,
    val stepsGoal: Int,
    val heartRateBpm: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val spO2: Int? = null,
    val weightKg: Int? = null,
    val heightCm: Int? = null,
    val isCycleRegular: Boolean? = null,
    val shortestCycleDays: Int? = null,
    val longestCycleDays: Int? = null,
    val hasHadUltrasound: Boolean? = null,
    val positiveTestDate: Date? = null,
    val scanDate: Date? = null,
    val scanWeeks: Int? = null,
    val scanDays: Int? = null,
    val pregnancyHistories: List<cit.edu.primera.feature.onboarding.domain.PregnancyHistory> = emptyList(),
    val isFirstPregnancy: Boolean? = null,
    val iceName: String? = null,
    val iceRelationship: String? = null,
    val icePrimaryPhone: String? = null,
    val iceSecondaryPhone: String? = null,
    val recentLogs: List<DashboardHealthLog>
)

data class DashboardHealthLog(
    val id: String? = null,
    val category: String,
    val description: String,
    val timestamp: Date
)
