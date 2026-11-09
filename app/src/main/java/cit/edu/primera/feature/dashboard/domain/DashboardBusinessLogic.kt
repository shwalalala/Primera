package cit.edu.primera.feature.dashboard.domain

import java.util.Calendar
import java.util.Date

object DashboardBusinessLogic {

    fun getTimeOfDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    private fun getGestationalDays(dueDate: Date?): Int {
        return cit.edu.primera.core.util.PregnancyCalculator.getGestationalDays(dueDate)
    }

    fun getWeekNumber(dueDate: Date?): Int {
        return cit.edu.primera.core.util.PregnancyCalculator.getGestationalWeeks(dueDate)
    }

    fun getDayNumber(dueDate: Date?): Int {
        return cit.edu.primera.core.util.PregnancyCalculator.getGestationalDayOfCurrentWeek(dueDate)
    }

    fun getDaysLeft(dueDate: Date?): Int {
        return cit.edu.primera.core.util.PregnancyCalculator.getDaysLeft(dueDate)
    }

    fun getTrimester(week: Int): String {
        return when (week) {
            in 1..13 -> "first trimester"
            in 14..27 -> "second trimester"
            else -> "third trimester"
        }
    }

    fun getBabySize(week: Int): String {
        return when (week) {
            1, 2, 3 -> "Your baby is in its earliest stages"
            4 -> "Poppy seed"
            5 -> "Sesame seed"
            6 -> "Lentil seed"
            7 -> "Blueberry"
            8 -> "Raspberry"
            9 -> "Grape"
            10 -> "Date"
            11 -> "Lime"
            12 -> "Plum"
            13 -> "Kiwi fruit"
            14 -> "Peach"
            15 -> "Pear"
            16 -> "Avocado"
            17 -> "Naval orange"
            18 -> "Pomegranate"
            19 -> "Grapefruit"
            20 -> "Mango"
            21 -> "Rockmelon"
            22, 23, 24 -> "Eggplant"
            25, 26, 27, 28 -> "Papaya"
            29, 30, 31, 32 -> "Pumpkin"
            33, 34, 35, 36 -> "Honeydew melon"
            37, 38, 39 -> "Watermelon"
            else -> "Your baby is full-term!"
        }
    }

    fun getBabyEmoji(week: Int): String {
        return when (week) {
            1, 2, 3 -> "✨"
            4 -> "🌱" // Poppy seed
            5 -> "🥯" // Sesame seed
            6 -> "🫘" // Lentil seed
            7 -> "🫐" // Blueberry
            8 -> "🍓" // Raspberry
            9 -> "🍇" // Grape
            10 -> "🌴" // Date
            11 -> "🍋" // Lime
            12 -> "🍑" // Plum
            13 -> "🥝" // Kiwi fruit
            14 -> "🍑" // Peach
            15 -> "🍐" // Pear
            16 -> "🥑" // Avocado
            17 -> "🍊" // Naval orange
            18 -> "🧅" // Pomegranate
            19 -> "🍊" // Grapefruit
            20 -> "🥭" // Mango
            21 -> "🍈" // Rockmelon
            22, 23, 24 -> "🍆" // Eggplant
            25, 26, 27, 28 -> "🥭" // Papaya
            29, 30, 31, 32 -> "🎃" // Pumpkin
            33, 34, 35, 36 -> "🍈" // Honeydew melon
            37, 38, 39 -> "🍉" // Watermelon
            else -> "👶"
        }
    }

    fun getBabyIllustration(week: Int): Int {
        return when (week) {
            1, 2, 3 -> cit.edu.primera.R.drawable.ic_size_earliest
            4 -> cit.edu.primera.R.drawable.ic_size_poppy_seed
            5 -> cit.edu.primera.R.drawable.ic_size_sesame_seed
            6 -> cit.edu.primera.R.drawable.ic_size_lentil_seed
            7 -> cit.edu.primera.R.drawable.ic_size_blueberry
            8 -> cit.edu.primera.R.drawable.ic_size_raspberry
            9 -> cit.edu.primera.R.drawable.ic_size_grape
            10 -> cit.edu.primera.R.drawable.ic_size_date
            11 -> cit.edu.primera.R.drawable.ic_size_lime
            12 -> cit.edu.primera.R.drawable.ic_size_plum
            13 -> cit.edu.primera.R.drawable.ic_size_kiwi
            14 -> cit.edu.primera.R.drawable.ic_size_peach
            15 -> cit.edu.primera.R.drawable.ic_size_pear
            16 -> cit.edu.primera.R.drawable.ic_size_avocado
            17 -> cit.edu.primera.R.drawable.ic_size_orange
            18 -> cit.edu.primera.R.drawable.ic_size_pomegranate
            19 -> cit.edu.primera.R.drawable.ic_size_grapefruit
            20 -> cit.edu.primera.R.drawable.ic_size_mango
            21 -> cit.edu.primera.R.drawable.ic_size_rockmelon
            22, 23, 24 -> cit.edu.primera.R.drawable.ic_size_eggplant
            25, 26, 27, 28 -> cit.edu.primera.R.drawable.ic_size_papaya
            29, 30, 31, 32 -> cit.edu.primera.R.drawable.ic_size_pumpkin
            33, 34, 35, 36 -> cit.edu.primera.R.drawable.ic_size_honeydew
            37, 38, 39 -> cit.edu.primera.R.drawable.ic_size_watermelon
            else -> cit.edu.primera.R.drawable.ic_size_fullterm
        }
    }

    fun getSleepQuality(hours: Int, minutes: Int): String {
        val total = hours * 60 + minutes
        return when {
            total >= 480 -> "Excellent quality"
            total >= 420 -> "Good quality"
            total >= 360 -> "Fair quality"
            else -> "Poor quality"
        }
    }

    fun getWeeklyMilestones(week: Int): List<String> {
        return BabyDevelopmentData.getInfoForWeek(week).milestones
    }

    fun getWeeklySymptoms(week: Int): List<String> {
        return BabyDevelopmentData.getInfoForWeek(week).symptoms
    }

    fun getWeeklyArticles(week: Int): List<ArticleInfo> {
        return BabyDevelopmentData.getInfoForWeek(week).articles
    }
}
