package com.example.primera.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

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
