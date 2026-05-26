package com.example.primera.frontend.features.dashboard

import androidx.compose.ui.graphics.Color

data class DashboardState(
    val userName: String = "Sarah",
    val timeOfDay: String = "Good morning",
    val trimester: String = "third trimester",
    val weekNumber: Int = 28,
    val dayNumber: Int = 3,
    val daysLeft: Int = 88,
    val babySize: String = "cabbage",
    val heartRateBpm: Int = 72,
    val heartRateTrendingUp: Boolean = true,
    val heartRateVsLastWeek: Int = 2,
    val steps: Int = 5_432,
    val stepsGoal: Int = 8_000,
    val sleepHours: Int = 7,
    val sleepMinutes: Int = 45,
    val sleepQuality: String = "Good quality",
    val wellnessGoals: List<WellnessGoalItem> = defaultWellnessGoals,
    val recentSymptoms: List<SymptomItem> = defaultSymptoms,
    val recentLogs: List<HealthLogItem> = defaultLogs,
    val weekDays: List<WeekDayItem> = defaultWeekDays,
    val isLoading: Boolean = false
)

data class WellnessGoalItem(
    val label: String,
    val emoji: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val barColor: Color
)

data class HealthLogItem(
    val category: String,
    val time: String,
    val description: String,
    val accentColor: Color   // left border + category label color
)

data class WeekDayItem(
    val initial: String,
    val date: Int,
    val isSelected: Boolean = false
)

data class SymptomItem(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

val defaultWellnessGoals = listOf(
    WellnessGoalItem("Hydration",     "💧", 1.8f, 2.5f, "L",   Color(0xFF7986CB)),
    WellnessGoalItem("Prenatal Yoga", "🧘", 15f,  30f,  "min", Color(0xFFBA68C8))
)

val defaultSymptoms = listOf(
    SymptomItem("Mild Nausea", Color(0xFFFFF3E0), Color(0xFFE65100)),
    SymptomItem("Back Pain",   Color(0xFFE8EAF6), Color(0xFF3949AB)),
    SymptomItem("Fatigue",     Color(0xFFF3E5F5), Color(0xFF7B1FA2))
)

val defaultLogs = listOf(
    HealthLogItem("Back Pain",      "8:32 AM",   "Felt mild lower back pain after walking this morning, around a 3 out of 10.", Color(0xFFEF9A9A)),
    HealthLogItem("Nutrition",      "7:10 AM",   "Had breakfast — oatmeal with banana. No nausea, feeling good overall.",       Color(0xFFA5D6A7)),
    HealthLogItem("Fetal Movement", "Yesterday", "Baby kicked a lot in the evening! Counted 12 movements in one hour.",         Color(0xFF9FA8DA))
)

val defaultWeekDays = listOf(
    WeekDayItem("S", 22),
    WeekDayItem("M", 23, isSelected = true),
    WeekDayItem("T", 24),
    WeekDayItem("W", 25),
    WeekDayItem("T", 26),
    WeekDayItem("F", 27),
    WeekDayItem("S", 28)
)