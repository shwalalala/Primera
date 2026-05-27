package com.example.primera.feature.onboarding.domain.model

import java.util.Date

data class OnboardingProfile(
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val birthday: Date? = null,
    val weightKg: Int = 50,
    val heightCm: Int = 160,
    val lmpDate: Date? = null,
    val eddDate: Date? = null,
    val isFirstPregnancy: Boolean? = null,
    val pregnancyNumber: Int = 1,
    val historyDeliveryDate: Date? = null,
    val deliveryType: String = "",
    val birthOutcome: String = "",
    val childrenDelivered: String = "",
    val complications: List<String> = emptyList()
)
