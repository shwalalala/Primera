package com.example.primera.frontend.features.dashboard

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class DashboardState(
    // ========== USER INFO (from profile) ==========
    val userName: String = "Sarah",
    val dueDate: Date? = null,

    // ========== DYNAMICALLY CALCULATED FIELDS ==========
    val timeOfDay: String = getTimeOfDay(),
    val trimester: String = getTrimester(dueDate),
    val weekNumber: Int = getWeekNumber(dueDate),
    val dayNumber: Int = getDayNumber(dueDate),
    val daysLeft: Int = getDaysLeft(dueDate),
    val babySize: String = getBabySize(getWeekNumber(dueDate)),

    // ========== STATIC PLACEHOLDER DATA ==========
    val heartRateBpm: Int = 72,
    val heartRateTrendingUp: Boolean = true,
    val heartRateVsLastWeek: Int = 2,
    val steps: Int = 5_432,
    val stepsGoal: Int = 8_000,
    val sleepHours: Int = 7,
    val sleepMinutes: Int = 45,
    val sleepQuality: String = getSleepQuality(sleepHours, sleepMinutes),
    val wellnessGoals: List<WellnessGoalItem> = defaultWellnessGoals,
    val recentSymptoms: List<SymptomItem> = defaultSymptoms,
    val recentLogs: List<HealthLogItem> = defaultLogs,
    val weekDays: List<WeekDayItem> = defaultWeekDays,
    val isLoading: Boolean = false
) {
    companion object {

        fun getTimeOfDay(): String {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 0..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
        }

        fun getWeekNumber(dueDate: Date?): Int {
            if (dueDate == null) return 28 // default fallback for prototype
            val today = Calendar.getInstance().time
            val diffInMillis = dueDate.time - today.time
            val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
            val daysPregnant = 280 - daysUntilDue
            return (daysPregnant / 7).coerceIn(1, 42)
        }

        fun getDayNumber(dueDate: Date?): Int {
            if (dueDate == null) return 3 // default fallback for prototype
            val today = Calendar.getInstance().time
            val diffInMillis = dueDate.time - today.time
            val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
            val daysPregnant = 280 - daysUntilDue
            return (daysPregnant % 7).coerceIn(0, 6)
        }

        fun getDaysLeft(dueDate: Date?): Int {
            if (dueDate == null) return 88 // default fallback for prototype
            val today = Calendar.getInstance().time
            val diffInMillis = dueDate.time - today.time
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
            return maxOf(0, days)
        }

        fun getTrimester(dueDate: Date?): String {
            val week = getWeekNumber(dueDate)
            return when (week) {
                in 1..13 -> "first trimester"
                in 14..27 -> "second trimester"
                else -> "third trimester"
            }
        }

        fun getBabySize(weekNumber: Int): String {
            return when (weekNumber) {
                1, 2, 3 -> "Microscopic"
                4 -> "Poppy seed"
                5 -> "Sesame seed"
                6 -> "Lentil"
                7 -> "Blueberry"
                8 -> "Raspberry"
                9 -> "Grape"
                10 -> "Strawberry"
                11 -> "Fig"
                12 -> "Lime"
                13 -> "Plum"
                14 -> "Lemon"
                15 -> "Apple"
                16 -> "Avocado"
                17 -> "Pomegranate"
                18 -> "Artichoke"
                19 -> "Mango"
                20 -> "Banana"
                21 -> "Carrot"
                22 -> "Papaya"
                23 -> "Mango"
                24 -> "Ear of corn"
                25 -> "Rutabaga"
                26 -> "Scallion"
                27 -> "Head of lettuce"
                28 -> "Large eggplant"
                29 -> "Acorn squash"
                30 -> "Cabbage"
                31 -> "Coconut"
                32 -> "Squash"
                33 -> "Pineapple"
                34 -> "Cantaloupe"
                35 -> "Honeydew melon"
                36 -> "Head of romaine lettuce"
                37 -> "Swiss chard"
                38 -> "Leek"
                39 -> "Mini watermelon"
                40 -> "Small pumpkin"
                41, 42 -> "Small pumpkin"
                else -> "Baby"
            }
        }

        fun getSleepQuality(hours: Int, minutes: Int): String {
            val totalMinutes = hours * 60 + minutes
            return when {
                totalMinutes >= 8 * 60 -> "Excellent quality"
                totalMinutes >= 7 * 60 -> "Good quality"
                totalMinutes >= 6 * 60 -> "Fair quality"
                else -> "Poor quality"
            }
        }
    }
}

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
    val accentColor: Color
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
