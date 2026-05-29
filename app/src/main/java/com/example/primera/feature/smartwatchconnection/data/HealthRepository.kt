package com.example.primera.feature.smartwatchconnection.data

import com.example.primera.feature.smartwatchconnection.data.toDto
import com.example.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HealthRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveSmartwatchHealth(
        smartwatchHealth: SmartwatchHealth
    ) {

        val currentUser = auth.currentUser
            ?: throw Exception("User is not authenticated")

        val userId = currentUser.uid

        val dto = smartwatchHealth.toDto()

        db.collection("users")
            .document(userId)
            .collection("smartwatchHealthRecords")
            .document(dto.date)
            .set(dto)
            .await()
    }
}