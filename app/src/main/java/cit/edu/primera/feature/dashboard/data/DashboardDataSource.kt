package cit.edu.primera.feature.dashboard.data

import cit.edu.primera.core.util.CryptoManager
import cit.edu.primera.core.util.currentUserIdFlow
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class DashboardDataSource(
    private val preferenceRepository: cit.edu.primera.core.data.PreferenceRepository? = null
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUserProfile(): Flow<UserDto?> = auth.currentUserIdFlow().flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(null)
        } else {
            callbackFlow {
                val registration: ListenerRegistration =
                    firestore.collection("users").document(userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(null)
                                return@addSnapshotListener
                            }

                            val userDto = if (snapshot != null && snapshot.exists()) {
                                try {
                                    val localPhoto = if (preferenceRepository != null) {
                                        preferenceRepository.getProfilePictureUri(userId)
                                    } else null
                                    val remotePhoto = snapshot.getString("photoUri")
                                    val resolvedPhotoUri =
                                        remotePhoto?.takeIf { it.isNotBlank() } ?: localPhoto

                                    val rawEdd = CryptoManager.decryptDate(snapshot.get("eddDate"))
                                        ?: CryptoManager.decryptDate(snapshot.get("dueDate"))
                                    val rawLmp = CryptoManager.decryptDate(snapshot.get("lmpDate"))
                                    val rawScanDate =
                                        CryptoManager.decryptDate(snapshot.get("scanDate"))
                                    val rawScanWeeks =
                                        CryptoManager.decryptInt(snapshot.get("scanWeeks"))
                                    val rawScanDays =
                                        CryptoManager.decryptInt(snapshot.get("scanDays"))
                                    val rawPositiveTestDate =
                                        CryptoManager.decryptDate(snapshot.get("positiveTestDate"))

                                    val resolvedDueDate =
                                        cit.edu.primera.core.util.PregnancyCalculator.calculateEdd(
                                            eddDate = rawEdd,
                                            scanDate = rawScanDate,
                                            scanWeeks = rawScanWeeks,
                                            scanDays = rawScanDays,
                                            lmpDate = rawLmp,
                                            positiveTestDate = rawPositiveTestDate
                                        )

                                    UserDto(
                                        email = snapshot.getString("email"),
                                        username = snapshot.getString("username"),
                                        photoUri = resolvedPhotoUri,
                                        firstName = CryptoManager.decrypt(snapshot.getString("firstName")),
                                        lastName = CryptoManager.decrypt(snapshot.getString("lastName")),
                                        middleName = CryptoManager.decrypt(snapshot.getString("middleName")),
                                        fullName = CryptoManager.decrypt(snapshot.getString("fullName")),
                                        birthday = CryptoManager.decryptDate(snapshot.get("birthday")),
                                        dueDate = resolvedDueDate,
                                        steps = snapshot.getLong("steps") ?: 0L,
                                        stepsGoal = snapshot.getLong("stepsGoal") ?: 8000L,
                                        heartRateBpm = snapshot.getLong("heartRateBpm") ?: 0L,
                                        sleepHours = snapshot.getLong("sleepHours") ?: 0L,
                                        sleepMinutes = snapshot.getLong("sleepMinutes") ?: 0L,
                                        spO2 = snapshot.getLong("spO2"),
                                        weightKg = CryptoManager.decryptLong(snapshot.get("weightKg")),
                                        heightCm = CryptoManager.decryptLong(snapshot.get("heightCm")),
                                        isCycleRegular = CryptoManager.decryptBoolean(snapshot.get("isCycleRegular")),
                                        hasHadUltrasound = CryptoManager.decryptBoolean(
                                            snapshot.get(
                                                "hasHadUltrasound"
                                            )
                                        ),
                                        shortestCycleDays = CryptoManager.decryptLong(snapshot.get("shortestCycleDays")),
                                        longestCycleDays = CryptoManager.decryptLong(snapshot.get("longestCycleDays")),
                                        positiveTestDate = rawPositiveTestDate,
                                        isFirstPregnancy = CryptoManager.decryptBoolean(
                                            snapshot.get(
                                                "isFirstPregnancy"
                                            )
                                        ),
                                        scanDate = rawScanDate,
                                        scanWeeks = rawScanWeeks?.toLong(),
                                        scanDays = rawScanDays?.toLong(),
                                        lmpDate = rawLmp,
                                        pregnancyHistories = CryptoManager.decryptJsonMapList(
                                            snapshot.get("pregnancyHistories")
                                        ),
                                        iceName = CryptoManager.decrypt(snapshot.getString("iceName")),
                                        iceRelationship = CryptoManager.decrypt(snapshot.getString("iceRelationship")),
                                        icePrimaryPhone = CryptoManager.decrypt(snapshot.getString("icePrimaryPhone")),
                                        iceSecondaryPhone = CryptoManager.decrypt(
                                            snapshot.getString(
                                                "iceSecondaryPhone"
                                            )
                                        )
                                    )
                                } catch (_: Exception) {
                                    null
                                }
                            } else {
                                null
                            }
                            trySend(userDto)
                        }

                awaitClose(registration::remove)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecentLogs(): Flow<List<HealthLogDto>> =
        auth.currentUserIdFlow().flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration: ListenerRegistration = firestore.collection("checkins")
                        .whereEqualTo("userId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(5)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }
                            val logs = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    HealthLogDto(
                                        id = doc.id,
                                        type = CryptoManager.decrypt(doc.getString("type")),
                                        category = CryptoManager.decrypt(doc.getString("category")),
                                        message = CryptoManager.decrypt(doc.getString("message")),
                                        description = CryptoManager.decrypt(doc.getString("description")),
                                        timestamp = doc.getTimestamp("timestamp")?.toDate(),
                                    )
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                            trySend(logs)
                        }
                    awaitClose(registration::remove)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeHealthRecords(): Flow<List<SmartwatchHealth>> =
        auth.currentUserIdFlow().flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration: ListenerRegistration = firestore.collection("users")
                        .document(userId)
                        .collection("smartwatchHealthRecords")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }
                            val records = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    SmartwatchHealth(
                                        date = doc.getString("date") ?: "",
                                        steps = doc.getLong("steps") ?: 0L,
                                        currentHeartRate = doc.getLong("currentHeartRate"),
                                        averageHeartRate = doc.getLong("averageHeartRate"),
                                        minimumHeartRate = doc.getLong("minimumHeartRate"),
                                        maximumHeartRate = doc.getLong("maximumHeartRate"),
                                        spO2 = doc.getDouble("spO2"),
                                        sleepMinutes = doc.getLong("sleepMinutes") ?: 0L,
                                        syncedAt = doc.getLong("syncedAt") ?: 0L
                                    )
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                            trySend(records)
                        }
                    awaitClose(registration::remove)
                }
            }
    }

    suspend fun saveHistoricalRecord(record: SmartwatchHealth): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val updates = mapOf(
                "date" to record.date,
                "steps" to record.steps,
                "currentHeartRate" to record.currentHeartRate,
                "averageHeartRate" to record.averageHeartRate,
                "minimumHeartRate" to record.minimumHeartRate,
                "maximumHeartRate" to record.maximumHeartRate,
                "spO2" to record.spO2,
                "sleepMinutes" to record.sleepMinutes,
                "syncedAt" to record.syncedAt
            )
            firestore.collection("users").document(userId)
                .collection("smartwatchHealthRecords")
                .document(record.date)
                .set(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStepsGoal(goal: Long): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            firestore.collection("users").document(userId)
                .update("stepsGoal", goal)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHealthData(
        steps: Long,
        heartRate: Long,
        sleepHours: Long,
        sleepMinutes: Long,
        spO2: Long? = null
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            android.util.Log.d("DashboardDataSource", "Updating health data for $userId: Steps=$steps, HR=$heartRate")
            
            val updates = mutableMapOf<String, Any>(
                "steps" to steps,
                "heartRateBpm" to heartRate,
                "sleepHours" to sleepHours,
                "sleepMinutes" to sleepMinutes,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            spO2?.let { updates["spO2"] = it }
            
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DashboardDataSource", "Failed to update health data", e)
            Result.failure(e)
        }
    }

    suspend fun updatePhotoUri(photoUri: String): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            firestore.collection("users").document(userId)
                .set(
                    mapOf("photoUri" to photoUri),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
