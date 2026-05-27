package com.example.primera.feature.profile.data.repository

import com.example.primera.feature.profile.domain.model.ProfileModel
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeUserProfile(): Flow<ProfileModel?>
}
