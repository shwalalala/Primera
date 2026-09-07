package cit.edu.primera.feature.dashboard.data
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DashboardDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun observeUserProfile(): Flow<UserDto?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                val userDto = if (snapshot != null && snapshot.exists()) {
                    try {
                        UserDto(
                            username = snapshot.getString("username"),
                            firstName = snapshot.getString("firstName"),
                            lastName = snapshot.getString("lastName"),
                            middleName = snapshot.getString("middleName"),
                            fullName = snapshot.getString("fullName"),
                            birthday = snapshot.getTimestamp("birthday")?.toDate(),
                            dueDate = (snapshot.getTimestamp("eddDate") ?: snapshot.getTimestamp("dueDate"))?.toDate(),
                            steps = snapshot.getLong("steps") ?: 0L,
                            stepsGoal = snapshot.getLong("stepsGoal") ?: 8000L,
                            heartRateBpm = snapshot.getLong("heartRateBpm") ?: 0L,
                            sleepHours = snapshot.getLong("sleepHours") ?: 0L,
                            sleepMinutes = snapshot.getLong("sleepMinutes") ?: 0L,
                            spO2 = snapshot.getLong("spO2"),
                            heightCm = snapshot.getLong("heightCm"),
                            isCycleRegular = snapshot.getBoolean("isCycleRegular"),
                            hasHadUltrasound = snapshot.getBoolean("hasHadUltrasound"),
                            shortestCycleDays = snapshot.getLong("shortestCycleDays"),
                            longestCycleDays = snapshot.getLong("longestCycleDays"),
                            positiveTestDate = snapshot.getTimestamp("positiveTestDate")?.toDate(),
                            isFirstPregnancy = snapshot.getBoolean("isFirstPregnancy"),
                            scanDate = snapshot.getTimestamp("scanDate")?.toDate(),
                            scanWeeks = snapshot.getLong("scanWeeks"),
                            scanDays = snapshot.getLong("scanDays"),
                            lmpDate = snapshot.getTimestamp("lmpDate")?.toDate(),
                            pregnancyHistories = snapshot.get("pregnancyHistories") as? List<Map<String, Any>>,
                            iceName = snapshot.getString("iceName"),
                            iceRelationship = snapshot.getString("iceRelationship"),
                            icePrimaryPhone = snapshot.getString("icePrimaryPhone"),
                            iceSecondaryPhone = snapshot.getString("iceSecondaryPhone")
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

    fun observeRecentLogs(): Flow<List<HealthLogDto>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

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
                            type = doc.getString("type"),
                            category = doc.getString("category"),
                            message = doc.getString("message"),
                            description = doc.getString("description"),
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

    fun observeHealthRecords(): Flow<List<SmartwatchHealth>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

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
}
