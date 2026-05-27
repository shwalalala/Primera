package com.example.primera.feature.onboarding.data.datasource

import com.example.primera.feature.onboarding.domain.model.OnboardingProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class OnboardingDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveProfile(profile: OnboardingProfile): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val data = hashMapOf(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "middleName" to profile.middleName,
                "birthday" to profile.birthday,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "lmpDate" to profile.lmpDate,
                "dueDate" to profile.eddDate,
                "isFirstPregnancy" to profile.isFirstPregnancy,
                "pregnancyNumber" to profile.pregnancyNumber,
                "historyDeliveryDate" to profile.historyDeliveryDate,
                "deliveryType" to profile.deliveryType,
                "birthOutcome" to profile.birthOutcome,
                "childrenDelivered" to profile.childrenDelivered,
                "complications" to profile.complications
            )
            firestore.collection("users").document(userId).update(data as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
