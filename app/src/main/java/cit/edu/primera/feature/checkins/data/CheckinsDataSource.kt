
package cit.edu.primera.feature.checkins.data

import cit.edu.primera.core.util.currentUserIdFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckinsDataSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ---------------------------------------------
    // DATE HELPERS
    // ---------------------------------------------

    private fun createDateFormat(): SimpleDateFormat =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

    private fun normalizeCheckinDate(
        dateString: String?,
        fallbackTimestamp: Date?
    ): String? {
        val formatter = createDateFormat()

        if (!dateString.isNullOrBlank()) {
            val parsed = runCatching {
                formatter.parse(dateString)
            }.getOrNull()

            if (
                parsed != null &&
                formatter.format(parsed) == dateString
            ) {
                return dateString
            }
        }

        return fallbackTimestamp?.let {
            formatter.format(it)
        }
    }

    // ---------------------------------------------
    // OBSERVE CHECK-IN HISTORY
    // ---------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLogs(): Flow<List<CheckinLogDto>> =
        auth.currentUserIdFlow().flatMapLatest { userId ->

            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration: ListenerRegistration =
                        firestore.collection("checkins")
                            .whereEqualTo("userId", userId)
                            .addSnapshotListener { snapshot, error ->

                                if (error != null) {
                                    trySend(emptyList())
                                    return@addSnapshotListener
                                }

                                val logs = snapshot?.documents
                                    ?.mapNotNull { doc ->
                                        try {
                                            CheckinLogDto(
                                                id = doc.id,
                                                type = doc.getString("type")
                                                    ?: "Check-in",
                                                category = doc.getString("category")
                                                    ?: "Fetal Movement",
                                                message = doc.getString("message"),
                                                description = doc.getString("description"),
                                                timestamp = doc.getTimestamp("timestamp")
                                                    ?.toDate(),

                                                // Legacy warning fields are
                                                // retained only for DTO
                                                // compatibility with old data.
                                                // No new warnings are generated.
                                                hasWarning = doc.getBoolean("hasWarning")
                                                    ?: false,
                                                warningMessage = doc.getString(
                                                    "warningMessage"
                                                ),

                                                checkinDate = doc.getString(
                                                    "checkinDate"
                                                )
                                            )
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    ?.sortedByDescending {
                                        it.timestamp ?: Date(0)
                                    }
                                    ?: emptyList()

                                trySend(logs)
                            }

                    awaitClose(registration::remove)
                }
            }
        }

    // ---------------------------------------------
    // OBSERVE USER WEIGHT
    // ---------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUserWeight(): Flow<CheckinUserDto?> =
        auth.currentUserIdFlow().flatMapLatest { userId ->

            if (userId.isNullOrBlank()) {
                flowOf(null)
            } else {
                callbackFlow {
                    val registration: ListenerRegistration =
                        firestore.collection("users")
                            .document(userId)
                            .addSnapshotListener { snapshot, error ->

                                if (error != null) {
                                    trySend(null)
                                    return@addSnapshotListener
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    val weightKg = snapshot
                                        .getLong("weightKg")
                                        ?.toInt()

                                    val updatedAt = snapshot
                                        .getTimestamp("updatedAt")
                                        ?.toDate()

                                    trySend(
                                        CheckinUserDto(
                                            weightKg,
                                            updatedAt
                                        )
                                    )
                                } else {
                                    trySend(null)
                                }
                            }

                    awaitClose(registration::remove)
                }
            }
        }

    // ---------------------------------------------
    // SAVE OR UPDATE DAILY CHECK-IN
    //
    // Symptom pattern detection has been removed.
    //
    // Check-ins are now recorded without medical
    // interpretation or symptom-warning generation.
    // ---------------------------------------------

    suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
        return try {
            // Preserve the existing authentication
            // behavior from the current data source.
            val userId = auth.currentUser?.uid ?: "anonymous"

            val currentCheckinDate = normalizeCheckinDate(
                dateString = log.checkinDate,
                fallbackTimestamp = log.timestamp ?: Date()
            )

            val logData = mapOf(
                "userId" to userId,
                "type" to log.type,
                "category" to log.category,
                "message" to log.message,
                "description" to log.description,
                "timestamp" to (log.timestamp ?: Date()),
                "checkinDate" to currentCheckinDate,

                // Keep legacy warning fields neutral.
                // This also clears them when an old
                // check-in is edited and saved.
                "hasWarning" to false,
                "warningMessage" to null
            )

            val checkinCollection =
                firestore.collection("checkins")

            val checkinReference =
                if (!log.id.isNullOrBlank()) {
                    checkinCollection.document(log.id)
                } else {
                    checkinCollection.document()
                }

            if (!log.id.isNullOrBlank()) {
                checkinReference
                    .set(
                        logData,
                        SetOptions.merge()
                    )
                    .await()
            } else {
                checkinReference
                    .set(logData)
                    .await()
            }

            // No symptom detection.
            // No recurring or persistent warnings.
            // No symptom notification creation.

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------------------------------------
    // UPDATE USER WEIGHT
    // Existing behavior preserved.
    // ---------------------------------------------

    suspend fun updateUserWeight(
        weightKg: Int
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: "anonymous"

            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "weightKg" to weightKg,
                        "updatedAt" to Date()
                    ),
                    SetOptions.merge()
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
