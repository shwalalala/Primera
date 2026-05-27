package com.example.primera.feature.profile.domain.model

import java.util.Date

data class ProfileModel(
    val fullName: String,
    val dueDate: Date?,
    val email: String
)
