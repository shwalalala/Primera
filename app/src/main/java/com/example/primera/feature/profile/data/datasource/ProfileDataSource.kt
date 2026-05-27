package com.example.primera.feature.profile.data.datasource

import com.example.primera.feature.profile.data.dto.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeUserProfile(userId: String): Flow<UserProfileDto?> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't close the flow with an exception to avoid crashing the app
                    // We could also send a special Error DTO if needed
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfileDto::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }
}
