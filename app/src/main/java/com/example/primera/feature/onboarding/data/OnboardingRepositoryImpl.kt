package com.example.primera.feature.onboarding.data

import com.example.primera.feature.onboarding.domain.OnboardingProfile
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
                "pregnancyHistories" to profile.pregnancyHistories.map { history ->
                    mapOf(
                        "pregnancyNumber" to history.pregnancyNumber,
                        "deliveryDate" to history.deliveryDate,
                        "deliveryType" to history.deliveryType,
                        "birthOutcome" to history.birthOutcome,
                        "childrenDelivered" to history.childrenDelivered,
                        "complications" to history.complications
                    )
                },
                "onboardingCompleted" to true,
                "updatedAt" to Date()
            )

            // Update the user document. Using merge(true) to avoid overwriting existing fields like email
            firestore.collection("users").document(userId)
                .update(profileData as Map<String, Any>)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
