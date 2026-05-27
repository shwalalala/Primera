package com.example.primera.feature.profile.data.repository

import com.example.primera.feature.auth.data.datasource.AuthDataSource
import com.example.primera.feature.profile.data.datasource.ProfileDataSource
import com.example.primera.feature.profile.domain.model.ProfileModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDataSource: ProfileDataSource,
    private val authDataSource: AuthDataSource
) : ProfileRepository {

    override fun observeUserProfile(): Flow<ProfileModel?> {
        val userId = authDataSource.getCurrentUserId() ?: return flowOf(null)
        
        return profileDataSource.observeUserProfile(userId).map { dto ->
            dto?.let {
                ProfileModel(
                    fullName = it.fullName ?: "Sarah",
                    dueDate = it.dueDate?.toDate(),
                    email = it.email ?: ""
                )
            }
        }
    }
}
