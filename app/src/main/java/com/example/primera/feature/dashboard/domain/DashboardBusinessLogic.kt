package com.example.primera.feature.dashboard.domain

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object DashboardBusinessLogic {

    fun getTimeOfDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun getWeekNumber(dueDate: Date?): Int {
        if (dueDate == null) return 0
        val diffInMillis = dueDate.time - System.currentTimeMillis()
        val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        return ((280 - daysUntilDue) / 7).coerceIn(1, 42)
    }

    fun getDayNumber(dueDate: Date?): Int {
        if (dueDate == null) return 0
        val diffInMillis = dueDate.time - System.currentTimeMillis()
        val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        return ((280 - daysUntilDue) % 7).coerceIn(0, 6)
    }

    fun getDaysLeft(dueDate: Date?): Int {
        if (dueDate == null) return 0
        val diffInMillis = dueDate.time - System.currentTimeMillis()
        return (TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()).coerceAtLeast(0)
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
            else -> "Baby"
        }
    }

    fun getBabyEmoji(week: Int): String {
        // TODO: Replace these emojis with actual 3D fruit/baby illustrations in the future
        return when (week) {
            1, 2, 3 -> "✨"
            4 -> "🌱" // Poppy seed
            5 -> "🥯" // Sesame seed
            6 -> "🫘" // Lentil
            7 -> "🫐" // Blueberry
            8 -> "🍓" // Raspberry
            9 -> "🍇" // Grape
            10 -> "🍓" // Strawberry
            11 -> "🫒" // Fig (using olive as fig emoji is rare)
            12 -> "🍋" // Lime
            13 -> "🍑" // Plum (using peach)
            14 -> "🍋" // Lemon
            15 -> "🍎" // Apple
            16 -> "🥑" // Avocado
            17 -> "🧅" // Pomegranate (using onion as placeholder)
            18 -> "🥦" // Artichoke
            19 -> "🥭" // Mango
            20 -> "🍌" // Banana
            21 -> "🥕" // Carrot
            22 -> "🥭" // Papaya
            23 -> "🥭" // Mango
            24 -> "🌽" // Ear of corn
            25 -> "🥦" // Rutabaga
            26 -> "🥬" // Scallion
            27 -> "🥬" // Head of lettuce
            28 -> "🍆" // Large eggplant
            29 -> "🎃" // Acorn squash
            30 -> "🥬" // Cabbage
            31 -> "🥥" // Coconut
            32 -> "🎃" // Squash
            33 -> "🍍" // Pineapple
            34 -> "🍈" // Cantaloupe
            35 -> "🍈" // Honeydew melon
            36 -> "🥬" // Head of romaine lettuce
            37 -> "🥬" // Swiss chard
            38 -> "🥬" // Leek
            39 -> "🍉" // Mini watermelon
            40 -> "🎃" // Small pumpkin
            else -> "👶"
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
}
