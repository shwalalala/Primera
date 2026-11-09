package cit.edu.primera.feature.notification.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<NotificationItem>>
    suspend fun addNotification(userId: String, notification: NotificationItem)
    suspend fun markAsRead(userId: String, notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun clearAllNotifications(userId: String)
}

class NotificationRepositoryImpl(
    private val context: Context
) : NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("primera_notifications_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATIONS_PREFIX = "notifications_json_"
    }

    override fun getNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val effectiveUserId = if (userId.isBlank()) {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } else userId

        if (effectiveUserId == "guest") {
            trySend(getLocalNotifications(effectiveUserId))
            awaitClose { }
            return@callbackFlow
        }

        val collectionRef = firestore.collection("users")
            .document(effectiveUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(getLocalNotifications(effectiveUserId))
                return@addSnapshotListener
            }

            val items = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val message = doc.getString("message") ?: return@mapNotNull null
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val category = doc.getString("category") ?: NotificationItem.CATEGORY_SYSTEM
                val destinationRoute = doc.getString("destinationRoute")
                val isRead = doc.getBoolean("isRead") ?: false

                NotificationItem(
                    id = doc.id,
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    category = category,
                    destinationRoute = destinationRoute,
                    isRead = isRead
                )
            }
            saveLocalNotifications(effectiveUserId, items)
            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addNotification(userId: String, notification: NotificationItem) {
        val effectiveUserId = if (userId.isBlank()) {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } else userId

        val currentLocal = getLocalNotifications(effectiveUserId).toMutableList()
        currentLocal.add(0, notification)
        saveLocalNotifications(effectiveUserId, currentLocal)

        if (effectiveUserId != "guest") {
            try {
                val data = mapOf(
                    "title" to notification.title,
                    "message" to notification.message,
                    "timestamp" to notification.timestamp,
                    "category" to notification.category,
                    "destinationRoute" to notification.destinationRoute,
                    "isRead" to notification.isRead
                )
                firestore.collection("users")
                    .document(effectiveUserId)
                    .collection("notifications")
                    .document(notification.id)
                    .set(data)
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        val effectiveUserId = if (userId.isBlank()) {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } else userId

        val currentLocal = getLocalNotifications(effectiveUserId).map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        saveLocalNotifications(effectiveUserId, currentLocal)

        if (effectiveUserId != "guest") {
            try {
                firestore.collection("users")
                    .document(effectiveUserId)
                    .collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        val effectiveUserId = if (userId.isBlank()) {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } else userId

        val currentLocal = getLocalNotifications(effectiveUserId).map { it.copy(isRead = true) }
        saveLocalNotifications(effectiveUserId, currentLocal)

        if (effectiveUserId != "guest") {
            try {
                val snapshot = firestore.collection("users")
                    .document(effectiveUserId)
                    .collection("notifications")
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun clearAllNotifications(userId: String) {
        val effectiveUserId = if (userId.isBlank()) {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } else userId

        prefs.edit { remove(KEY_NOTIFICATIONS_PREFIX + effectiveUserId) }

        if (effectiveUserId != "guest") {
            try {
                val snapshot = firestore.collection("users")
                    .document(effectiveUserId)
                    .collection("notifications")
                    .get()
                    .await()

                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            } catch (_: Exception) {
            }
        }
    }

    private fun getLocalNotifications(userId: String): List<NotificationItem> {
        val jsonStr = prefs.getString(KEY_NOTIFICATIONS_PREFIX + userId, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<NotificationItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NotificationItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        message = obj.optString("message"),
                        timestamp = obj.optLong("timestamp"),
                        category = obj.optString("category", NotificationItem.CATEGORY_SYSTEM),
                        destinationRoute = if (obj.has("destinationRoute") && !obj.isNull("destinationRoute")) obj.getString("destinationRoute") else null,
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLocalNotifications(userId: String, items: List<NotificationItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("message", item.message)
                put("timestamp", item.timestamp)
                put("category", item.category)
                put("destinationRoute", item.destinationRoute)
                put("isRead", item.isRead)
            }
            array.put(obj)
        }
        prefs.edit { putString(KEY_NOTIFICATIONS_PREFIX + userId, array.toString()) }
    }
}
