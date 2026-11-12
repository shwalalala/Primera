
package cit.edu.primera.feature.checkins.data

import cit.edu.primera.core.util.currentUserIdFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

class PrenatalDataSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val prenatalCollection
        get() = firestore.collection("prenatal_checkups")

    // --------------------------------------------
    // REAL-TIME PRENATAL HISTORY
    // --------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecords(): Flow<List<PrenatalRecordDto>> =
        auth.currentUserIdFlow().flatMapLatest { userId ->

            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration: ListenerRegistration =
                        prenatalCollection
                            .whereEqualTo("userId", userId)
                            .addSnapshotListener { snapshot, error ->

                                if (error != null) {
                                    close(error)
                                    return@addSnapshotListener
                                }

                                val records = snapshot?.documents
                                    ?.mapNotNull { document ->
                                        try {
                                            val details =
                                                (document.get("details") as? Map<*, *>)
                                                    ?.entries
                                                    ?.mapNotNull { entry ->
                                                        val key = entry.key as? String
                                                        val value = entry.value as? String
                                                        if (key != null && value != null) {
                                                            key to value
                                                        } else {
                                                            null
                                                        }
                                                    }
                                                    ?.toMap()
                                                    ?: emptyMap()

                                            PrenatalRecordDto(
                                                id = document.id,
                                                userId = document.getString("userId")
                                                    ?: userId,
                                                visitDate = document.getString("visitDate")
                                                    .orEmpty(),
                                                status = document.getString("status")
                                                    ?: "scheduled",
                                                details = details,
                                                createdAt = document.getTimestamp("createdAt")
                                                    ?.toDate(),
                                                updatedAt = document.getTimestamp("updatedAt")
                                                    ?.toDate()
                                            )
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    ?.sortedWith(
                                        compareByDescending<PrenatalRecordDto> {
                                            it.status == "scheduled"
                                        }.thenByDescending {
                                            it.visitDate
                                        }
                                    )
                                    ?: emptyList()

                                trySend(records)
                            }

                    awaitClose(registration::remove)
                }
            }
        }

    // --------------------------------------------
    // CREATE PRENATAL RECORD
    // --------------------------------------------

    suspend fun createRecord(
        record: PrenatalRecordDto
    ): Result<String> {
        return try {
            val userId = requireSignedInUserId()
            validateRecord(record)

            val now = Date()

            val data = mapOf(
                "userId" to userId,
                "visitDate" to record.visitDate,
                "status" to record.status,
                "details" to record.details,
                "createdAt" to now,
                "updatedAt" to now
            )

            val document = prenatalCollection
                .add(data)
                .await()

            Result.success(document.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------
    // UPDATE EXISTING PRENATAL RECORD
    // --------------------------------------------

    suspend fun updateRecord(
        record: PrenatalRecordDto
    ): Result<Unit> {
        return try {
            val userId = requireSignedInUserId()
            validateRecord(record)

            val recordId = requireNotNull(
                record.id?.takeIf { it.isNotBlank() }
            ) {
                "A record ID is required to update a prenatal record."
            }

            val reference = prenatalCollection.document(recordId)
            val existing = reference.get().await()

            require(existing.exists()) {
                "Prenatal record not found."
            }

            require(existing.getString("userId") == userId) {
                "You cannot modify another user's record."
            }

            reference.update(
                mapOf(
                    "visitDate" to record.visitDate,
                    "status" to record.status,
                    "details" to record.details,
                    "updatedAt" to Date()
                )
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------
    // CANCEL APPOINTMENT
    // --------------------------------------------

    suspend fun cancelAppointment(
        recordId: String
    ): Result<Unit> {
        return try {
            val userId = requireSignedInUserId()
            val reference = prenatalCollection.document(recordId)
            val existing = reference.get().await()

            require(existing.exists()) {
                "Appointment not found."
            }

            require(existing.getString("userId") == userId) {
                "You cannot cancel another user's appointment."
            }

            require(existing.getString("status") == "scheduled") {
                "Only scheduled appointments can be cancelled."
            }

            reference.update(
                mapOf(
                    "status" to "cancelled",
                    "updatedAt" to Date()
                )
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------
    // DELETE PRENATAL RECORD
    // --------------------------------------------

    suspend fun deleteRecord(
        recordId: String
    ): Result<Unit> {
        return try {
            val userId = requireSignedInUserId()
            val reference = prenatalCollection.document(recordId)
            val existing = reference.get().await()

            require(existing.exists()) {
                "Prenatal record not found."
            }

            require(existing.getString("userId") == userId) {
                "You cannot delete another user's record."
            }

            reference.delete().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------
    // VALIDATION AND AUTHENTICATION
    // --------------------------------------------

    private fun requireSignedInUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException(
                "You must be signed in to manage prenatal records."
            )
    }

    private fun validateRecord(record: PrenatalRecordDto) {
        require(record.status in setOf(
            "scheduled", "completed", "cancelled"
        )) {
            "Invalid prenatal record status."
        }

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        val parsed = formatter.parse(record.visitDate)

        require(
            parsed != null &&
                    formatter.format(parsed) == record.visitDate
        ) {
            "Enter a valid visit date (YYYY-MM-DD)."
        }

        val today = formatter.format(Date())

        if (record.status == "scheduled") {
            require(record.visitDate >= today) {
                "A new appointment cannot be scheduled in the past."
            }
        }

        if (record.status == "completed") {
            require(record.visitDate <= today) {
                "Completed checkups cannot have a future date."
            }
        }
    }
}
