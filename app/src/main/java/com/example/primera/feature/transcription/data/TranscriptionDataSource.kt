package com.example.primera.feature.transcription.data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TranscriptionDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun saveTranscription(dto: TranscriptionDto): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val transcriptionWithUser = dto.copy(userId = userId)
            
            val documentReference = firestore.collection("transcriptions")
                .add(transcriptionWithUser)
                .await()
            
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
