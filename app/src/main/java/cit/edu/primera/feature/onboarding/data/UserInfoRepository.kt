package cit.edu.primera.feature.onboarding.data

import cit.edu.primera.core.util.CryptoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class UserInfoRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
                "firstName" to CryptoManager.encrypt(firstName),
                "lastName" to CryptoManager.encrypt(lastName),
                "middleName" to CryptoManager.encrypt(middleName),
                "birthday" to CryptoManager.encryptDate(birthday),
                "weightKg" to CryptoManager.encryptInt(weightKg),
                "heightCm" to CryptoManager.encryptInt(heightCm),
                "lmpDate" to CryptoManager.encryptDate(lmpDate),
                "eddDate" to CryptoManager.encryptDate(eddDate),
                "isFirstPregnancy" to CryptoManager.encryptBoolean(isFirstPregnancy),
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
                "pregnancyNumber" to CryptoManager.encryptInt(pregnancyNumber),
                "historyDeliveryDate" to CryptoManager.encryptDate(historyDeliveryDate),
                "deliveryType" to CryptoManager.encrypt(deliveryType),
                "childrenDelivered" to CryptoManager.encrypt(childrenDelivered),
                "complications" to CryptoManager.encryptStringList(complications),
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
