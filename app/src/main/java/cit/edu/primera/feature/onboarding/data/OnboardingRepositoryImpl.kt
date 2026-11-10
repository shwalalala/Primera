package cit.edu.primera.feature.onboarding.data

import cit.edu.primera.core.util.CryptoManager
import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

class OnboardingRepositoryImpl : OnboardingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun saveProfile(profile: OnboardingProfile): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val userEmail = profile.email?.takeIf { it.isNotBlank() } ?: auth.currentUser?.email

            val pregnancyHistoriesMaps = profile.pregnancyHistories.map { history ->
                mapOf(
                    "pregnancyNumber" to CryptoManager.encryptInt(history.pregnancyNumber),
                    "deliveryDate" to CryptoManager.encryptDate(history.deliveryDate),
                    "deliveryType" to CryptoManager.encrypt(history.deliveryType),
                    "birthOutcome" to CryptoManager.encrypt(history.birthOutcome),
                    "childrenDelivered" to CryptoManager.encrypt(history.childrenDelivered),
                    "areChildrenLiving" to CryptoManager.encryptBoolean(history.areChildrenLiving),
                    "hasGdm" to CryptoManager.encryptBoolean(history.hasGdm),
                    "hasDiabetes" to CryptoManager.encryptBoolean(history.hasDiabetes),
                    "hasPreeclampsia" to CryptoManager.encryptBoolean(history.hasPreeclampsia),
                    "hasPretermDelivery" to CryptoManager.encryptBoolean(history.hasPretermDelivery),
                    "additionalComplications" to CryptoManager.encrypt(history.additionalComplications),
                    "complications" to CryptoManager.encryptStringList(history.complications)
                )
            }

            val profileData = hashMapOf<String, Any?>(
                "email" to userEmail,
                "username" to profile.username,
                "birthday" to CryptoManager.encryptDate(profile.birthday),
                "weightKg" to CryptoManager.encryptInt(profile.weightKg),
                "heightCm" to CryptoManager.encryptInt(profile.heightCm),
                "isCycleRegular" to CryptoManager.encryptBoolean(profile.isCycleRegular),
                "shortestCycleDays" to CryptoManager.encryptInt(profile.shortestCycleDays),
                "longestCycleDays" to CryptoManager.encryptInt(profile.longestCycleDays),
                "hasHadUltrasound" to CryptoManager.encryptBoolean(profile.hasHadUltrasound),
                "positiveTestDate" to CryptoManager.encryptDate(profile.positiveTestDate),
                "lmpDate" to CryptoManager.encryptDate(profile.lmpDate),
                "eddDate" to CryptoManager.encryptDate(profile.eddDate),
                "isFirstPregnancy" to CryptoManager.encryptBoolean(profile.isFirstPregnancy),
                "scanDate" to CryptoManager.encryptDate(profile.scanDate),
                "scanWeeks" to CryptoManager.encryptInt(profile.scanWeeks),
                "scanDays" to CryptoManager.encryptInt(profile.scanDays),
                "iceName" to CryptoManager.encrypt(profile.iceName),
                "iceRelationship" to CryptoManager.encrypt(profile.iceRelationship),
                "icePrimaryPhone" to CryptoManager.encrypt(profile.icePrimaryPhone),
                "iceSecondaryPhone" to CryptoManager.encrypt(profile.iceSecondaryPhone),
                "pregnancyHistories" to CryptoManager.encryptJsonMapList(pregnancyHistoriesMaps),
                "onboardingCompleted" to true,
                "updatedAt" to Date()
            )

            // Update the user document. Using merge(true) to avoid failing if user document isn't fully created yet
            firestore.collection("users").document(userId)
                .set(profileData as Map<String, Any>, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
