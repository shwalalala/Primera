package com.example.primera.feature.transcription.domain

data class TranscriptionModel(
    val transcribedText: String,
    val audioDuration: Long,
    val confidenceScore: Float,
    val languageCode: String,
    val deviceModel: String,
    val appVersion: String,
    val userId: String,
    val timestamp: Long
) {
    val confidencePercent: String 
        get() = "${(confidenceScore * 100).toInt()}%"
}
