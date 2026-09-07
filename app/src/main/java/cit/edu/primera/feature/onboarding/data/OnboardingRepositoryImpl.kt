package cit.edu.primera.feature.onboarding.data

import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
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

            val profileData = hashMapOf<String, Any?>(
                "email" to profile.email,
                "birthday" to profile.birthday,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "isCycleRegular" to profile.isCycleRegular,
                "shortestCycleDays" to profile.shortestCycleDays,
                "longestCycleDays" to profile.longestCycleDays,
                "hasHadUltrasound" to profile.hasHadUltrasound,
                "positiveTestDate" to profile.positiveTestDate,
                "lmpDate" to profile.lmpDate,
                "eddDate" to profile.eddDate,
                "isFirstPregnancy" to profile.isFirstPregnancy,
                "scanDate" to profile.scanDate,
                "scanWeeks" to profile.scanWeeks,
                "scanDays" to profile.scanDays,
                "iceName" to profile.iceName,
                "iceRelationship" to profile.iceRelationship,
                "icePrimaryPhone" to profile.icePrimaryPhone,
                "iceSecondaryPhone" to profile.iceSecondaryPhone,
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
