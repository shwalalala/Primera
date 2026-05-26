package com.example.primera.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DashboardRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun observeUserProfile(onUpdate: (Map<String, Any?>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: return null
        
        return firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    onUpdate(snapshot.data ?: emptyMap())
                }
            }
    }

    fun observeRecentLogs(onUpdate: (List<Map<String, Any?>>) -> Unit): ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: return null

        return firestore.collection("activity_logs")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val logs = snapshot?.documents?.map { doc ->
                    doc.data ?: emptyMap()
                } ?: emptyList()
                
                onUpdate(logs)
            }
    }
}
