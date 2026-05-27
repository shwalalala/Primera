package com.example.primera.feature.transcription.data.dto

import com.example.primera.feature.transcription.domain.model.TranscriptionModel
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TranscriptionDto(
    @get:JvmName("getTranscribedText") val transcribedText: String = "",
    @get:JvmName("getAudioDuration") val audioDuration: Long = 0L,
    @get:JvmName("getConfidenceScore") val confidenceScore: Float = 0.0f,
    @get:JvmName("getLanguageCode") val languageCode: String = "",
    @get:JvmName("getDeviceModel") val deviceModel: String = "",
    @get:JvmName("getAppVersion") val appVersion: String = "",
    @get:JvmName("getUserId") val userId: String = "",
    @ServerTimestamp val timestamp: Date? = null
) {
    fun toModel() = TranscriptionModel(
        transcribedText = transcribedText,
        audioDuration = audioDuration,
        confidenceScore = confidenceScore,
        languageCode = languageCode,
        deviceModel = deviceModel,
        appVersion = appVersion,
        userId = userId,
        timestamp = timestamp?.time ?: System.currentTimeMillis()
    )
}

fun TranscriptionModel.toDto() = TranscriptionDto(
    transcribedText = transcribedText,
    audioDuration = audioDuration,
    confidenceScore = confidenceScore,
    languageCode = languageCode,
    deviceModel = deviceModel,
    appVersion = appVersion,
    userId = userId,
    timestamp = null // Firestore will populate this via @ServerTimestamp
)
