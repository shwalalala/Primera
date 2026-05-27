package com.example.primera.core.utils

import java.util.*
import java.util.concurrent.TimeUnit

object PregnancyCalculator {

    fun getWeekNumber(dueDate: Date?): Int {
        if (dueDate == null) return 28
        val today = Calendar.getInstance().time
        val diffInMillis = dueDate.time - today.time
        val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        val daysPregnant = 280 - daysUntilDue
        return (daysPregnant / 7).coerceIn(1, 42)
    }

    fun getDayNumber(dueDate: Date?): Int {
        if (dueDate == null) return 3
        val today = Calendar.getInstance().time
        val diffInMillis = dueDate.time - today.time
        val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        val daysPregnant = 280 - daysUntilDue
        return (daysPregnant % 7).coerceIn(0, 6)
    }

    fun getDaysLeft(dueDate: Date?): Int {
        if (dueDate == null) return 88
        val today = Calendar.getInstance().time
        val diffInMillis = dueDate.time - today.time
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        return maxOf(0, days)
    }

    fun getTrimester(week: Int): String {
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
}
