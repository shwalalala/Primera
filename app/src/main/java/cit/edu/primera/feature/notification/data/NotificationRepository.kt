
package cit.edu.primera.feature.notification.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    suspend fun addNotification(
        userId: String,
        notification: NotificationItem
    )
    suspend fun markAsRead(
        userId: String,
        notificationId: String
    )
    suspend fun markAllAsRead(userId: String)
    suspend fun clearAllNotifications(userId: String)
}

class NotificationRepositoryImpl(
    private val context: Context
) : NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "primera_notifications_prefs",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val TAG = "NotificationRepository"

        private const val KEY_NOTIFICATIONS_PREFIX =
            "notifications_json_"

        private const val KEY_PENDING_PREFIX =
            "pending_notification_ids_"
    }

    // ---------------------------------------------
    // USER ID
    // ---------------------------------------------

    private fun resolveUserId(userId: String): String {
        return if (userId.isBlank()) {
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: "guest"
        } else {
            userId
        }
    }

    private fun notificationsKey(userId: String): String {
        return KEY_NOTIFICATIONS_PREFIX + userId
    }

    private fun pendingKey(userId: String): String {
        return KEY_PENDING_PREFIX + userId
    }

    private fun getPendingIds(
        userId: String
    ): Set<String> {
        return prefs.getStringSet(
            pendingKey(userId),
            emptySet()
        )?.toSet() ?: emptySet()
    }

    private fun addPendingId(
        userId: String,
        notificationId: String
    ) {
        val pending = getPendingIds(userId)
            .toMutableSet()

        pending.add(notificationId)

        prefs.edit {
            putStringSet(
                pendingKey(userId),
                pending
            )
        }
    }

    private fun removePendingId(
        userId: String,
        notificationId: String
    ) {
        val pending = getPendingIds(userId)
            .toMutableSet()

        pending.remove(notificationId)

        prefs.edit {
            putStringSet(
                pendingKey(userId),
                pending
            )
        }
    }

    // ---------------------------------------------
    // OBSERVE NOTIFICATIONS
    //
    // Listen to both:
    // 1. Local notification changes
    // 2. Firestore notification changes
    //
    // This allows reminders saved by a different
    // repository instance to appear in the UI.
    // ---------------------------------------------

    override fun getNotifications(
        userId: String
    ): Flow<List<NotificationItem>> = callbackFlow {

        val effectiveUserId = resolveUserId(userId)
        val localKey = notificationsKey(effectiveUserId)
        val pendingStorageKey = pendingKey(effectiveUserId)

        // Immediately display existing local history.
        trySend(getLocalNotifications(effectiveUserId))

        // The milestone notifier may use another
        // NotificationRepositoryImpl instance.
        //
        // SharedPreferences listeners let this flow
        // respond when that instance saves locally.

        val preferenceListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    _, changedKey ->

                if (
                    changedKey == localKey ||
                    changedKey == pendingStorageKey
                ) {
                    trySend(
                        getLocalNotifications(effectiveUserId)
                    )
                }
            }

        prefs.registerOnSharedPreferenceChangeListener(
            preferenceListener
        )

        if (effectiveUserId == "guest") {
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(
                    preferenceListener
                )
            }
            return@callbackFlow
        }

        val collectionRef = firestore
            .collection("users")
            .document(effectiveUserId)
            .collection("notifications")
            .orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )

        val firestoreListener =
            collectionRef.addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {

                    Log.w(
                        TAG,
                        "Firestore notification listener unavailable",
                        error
                    )

                    trySend(
                        getLocalNotifications(effectiveUserId)
                    )

                    return@addSnapshotListener
                }

                val remoteItems =
                    snapshot.documents.mapNotNull { doc ->

                        val title =
                            doc.getString("title")
                                ?: return@mapNotNull null

                        val message =
                            doc.getString("message")
                                ?: return@mapNotNull null

                        NotificationItem(
                            id = doc.id,
                            title = title,
                            message = message,
                            timestamp =
                                doc.getLong("timestamp")
                                    ?: System.currentTimeMillis(),
                            category =
                                doc.getString("category")
                                    ?: NotificationItem.CATEGORY_SYSTEM,
                            destinationRoute =
                                doc.getString("destinationRoute"),
                            isRead =
                                doc.getBoolean("isRead") ?: false
                        )
                    }

                // Preserve locally created notifications
                // while their Firestore writes are pending.
                //
                // Without this step, an older remote
                // snapshot could erase a newly created
                // pregnancy milestone from local history.

                val pendingIds =
                    getPendingIds(effectiveUserId)

                val pendingLocalItems =
                    getLocalNotifications(effectiveUserId)
                        .filter { item ->
                            item.id in pendingIds
                        }

                // Pending local versions take priority
                // over older remote versions.

                val mergedItems =
                    (pendingLocalItems + remoteItems)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }

                saveLocalNotifications(
                    effectiveUserId,
                    mergedItems
                )

                trySend(mergedItems)
            }

        awaitClose {
            firestoreListener.remove()

            prefs.unregisterOnSharedPreferenceChangeListener(
                preferenceListener
            )
        }
    }

    // ---------------------------------------------
    // ADD NOTIFICATION
    // ---------------------------------------------

    override suspend fun addNotification(
        userId: String,
        notification: NotificationItem
    ) {
        val effectiveUserId = resolveUserId(userId)

        // Mark as pending before writing locally,
        // so a Firestore snapshot cannot remove it
        // before the remote write completes.

        if (effectiveUserId != "guest") {
            addPendingId(
                effectiveUserId,
                notification.id
            )
        }

        val currentLocal =
            getLocalNotifications(effectiveUserId)
                .toMutableList()

        // A deterministic notification ID allows
        // updating the existing notification instead
        // of adding a duplicate.

        currentLocal.removeAll {
            it.id == notification.id
        }

        currentLocal.add(0, notification)

        saveLocalNotifications(
            effectiveUserId,
            currentLocal
        )

        Log.d(
            TAG,
            "Notification saved locally: " +
                    "category=${notification.category}, " +
                    "id=${notification.id}"
        )

        if (effectiveUserId == "guest") {
            return
        }

        try {
            val data = mapOf(
                "title" to notification.title,
                "message" to notification.message,
                "timestamp" to notification.timestamp,
                "category" to notification.category,
                "destinationRoute" to notification.destinationRoute,
                "isRead" to notification.isRead
            )

            firestore
                .collection("users")
                .document(effectiveUserId)
                .collection("notifications")
                .document(notification.id)
                .set(data)
                .await()

            removePendingId(
                effectiveUserId,
                notification.id
            )

            Log.d(
                TAG,
                "Notification synchronized: ${notification.id}"
            )

        } catch (e: Exception) {

            // Keep the local notification and its
            // pending ID, rather than deleting it.

            Log.e(
                TAG,
                "Failed to synchronize notification: " +
                        notification.id,
                e
            )
        }
    }

    // ---------------------------------------------
    // MARK ONE AS READ
    // ---------------------------------------------

    override suspend fun markAsRead(
        userId: String,
        notificationId: String
    ) {
        val effectiveUserId = resolveUserId(userId)

        val currentLocal =
            getLocalNotifications(effectiveUserId)
                .map { item ->

                    if (item.id == notificationId) {
                        item.copy(isRead = true)
                    } else {
                        item
                    }
                }

        saveLocalNotifications(
            effectiveUserId,
            currentLocal
        )

        if (effectiveUserId == "guest") {
            return
        }

        try {
            firestore
                .collection("users")
                .document(effectiveUserId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to mark notification as read",
                e
            )
        }
    }

    // ---------------------------------------------
    // MARK ALL AS READ
    // ---------------------------------------------

    override suspend fun markAllAsRead(
        userId: String
    ) {
        val effectiveUserId = resolveUserId(userId)

        val currentLocal =
            getLocalNotifications(effectiveUserId)
                .map { it.copy(isRead = true) }

        saveLocalNotifications(
            effectiveUserId,
            currentLocal
        )

        if (effectiveUserId == "guest") {
            return
        }

        try {
            val snapshot = firestore
                .collection("users")
                .document(effectiveUserId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()

            for (doc in snapshot.documents) {
                batch.update(
                    doc.reference,
                    "isRead",
                    true
                )
            }

            batch.commit().await()

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to mark all notifications as read",
                e
            )
        }
    }

    // ---------------------------------------------
    // CLEAR ALL
    // ---------------------------------------------

    override suspend fun clearAllNotifications(
        userId: String
    ) {
        val effectiveUserId = resolveUserId(userId)

        prefs.edit {
            remove(notificationsKey(effectiveUserId))
            remove(pendingKey(effectiveUserId))
        }

        if (effectiveUserId == "guest") {
            return
        }

        try {
            val snapshot = firestore
                .collection("users")
                .document(effectiveUserId)
                .collection("notifications")
                .get()
                .await()

            val batch = firestore.batch()

            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }

            batch.commit().await()

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to clear remote notifications",
                e
            )
        }
    }

    // ---------------------------------------------
    // READ LOCAL NOTIFICATIONS
    // ---------------------------------------------

    private fun getLocalNotifications(
        userId: String
    ): List<NotificationItem> {

        val jsonStr =
            prefs.getString(
                notificationsKey(userId),
                null
            ) ?: return emptyList()

        return try {

            val array = JSONArray(jsonStr)

            val list =
                mutableListOf<NotificationItem>()

            for (i in 0 until array.length()) {

                val obj = array.getJSONObject(i)

                list.add(
                    NotificationItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        message = obj.optString("message"),
                        timestamp = obj.optLong(
                            "timestamp"
                        ),
                        category = obj.optString(
                            "category",
                            NotificationItem.CATEGORY_SYSTEM
                        ),
                        destinationRoute =
                            if (
                                obj.has("destinationRoute") &&
                                !obj.isNull("destinationRoute")
                            ) {
                                obj.getString(
                                    "destinationRoute"
                                )
                            } else {
                                null
                            },
                        isRead = obj.optBoolean(
                            "isRead",
                            false
                        )
                    )
                )
            }

            list.sortedByDescending {
                it.timestamp
            }

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to parse local notifications",
                e
            )

            emptyList()
        }
    }

    // ---------------------------------------------
    // SAVE LOCAL NOTIFICATIONS
    // ---------------------------------------------

    private fun saveLocalNotifications(
        userId: String,
        items: List<NotificationItem>
    ) {
        val array = JSONArray()

        val sortedItems =
            items.sortedByDescending {
                it.timestamp
            }

        for (item in sortedItems) {

            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("message", item.message)
                put("timestamp", item.timestamp)
                put("category", item.category)
                put(
                    "destinationRoute",
                    item.destinationRoute
                )
                put("isRead", item.isRead)
            }

            array.put(obj)
        }

        val encoded = array.toString()
        val key = notificationsKey(userId)

        // Avoid unnecessary preference writes
        // and repeated listener notifications.

        if (prefs.getString(key, null) != encoded) {
            prefs.edit {
                putString(key, encoded)
            }
        }
    }
}
