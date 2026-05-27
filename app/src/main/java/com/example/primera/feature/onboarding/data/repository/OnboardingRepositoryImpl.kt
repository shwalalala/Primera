package com.example.primera.feature.onboarding.data.repository

import com.example.primera.feature.onboarding.domain.model.OnboardingProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class OnboardingRepositoryImpl : OnboardingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun saveProfile(profile: OnboardingProfile): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val profileData = hashMapOf(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "middleName" to profile.middleName,
                "birthday" to profile.birthday,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "lmpDate" to profile.lmpDate,
                "eddDate" to profile.eddDate,
                "isFirstPregnancy" to profile.isFirstPregnancy,
                "pregnancyNumber" to profile.pregnancyNumber,
                "historyDeliveryDate" to profile.historyDeliveryDate,
                "deliveryType" to profile.deliveryType,
                "birthOutcome" to profile.birthOutcome,
                "childrenDelivered" to profile.childrenDelivered,
                "complications" to profile.complications,
                "updatedAt" to Date()
            )

            firestore.collection("users").document(userId)
                .set(profileData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserInfo(
        firstName: String,
        lastName: String,
        middleName: String?,
        birthday: Date,
        weightKg: Int,
        heightCm: Int,
        lmpDate: Date?,
        eddDate: Date?,
        isFirstPregnancy: Boolean
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val userInfo = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "middleName" to middleName,
                "birthday" to birthday,
                "weightKg" to weightKg,
                "heightCm" to heightCm,
                "lmpDate" to lmpDate,
                "eddDate" to eddDate,
                "isFirstPregnancy" to isFirstPregnancy,
                "updatedAt" to Date()
            )

            firestore.collection("users").document(userId)
                .update(userInfo as Map<String, Any>)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePregnancyHistory(
        pregnancyNumber: Int,
        historyDeliveryDate: Date,
        deliveryType: String,
        childrenDelivered: String,
        complications: List<String>
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val historyData = hashMapOf(
                "pregnancyNumber" to pregnancyNumber,
                "historyDeliveryDate" to historyDeliveryDate,
                "deliveryType" to deliveryType,
                "childrenDelivered" to childrenDelivered,
                "complications" to complications,
                "updatedAt" to Date()
            )

            firestore.collection("users").document(userId)
                .collection("pregnancy_history")
                .add(historyData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
