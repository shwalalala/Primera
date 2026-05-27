package com.example.primera.feature.wellness.domain.model

data class WellnessGoalModel(
    val label: String,
    val emoji: String,
    val current: Float,
    val target: Float,
    val unit: String,
    val hexColor: String
)
