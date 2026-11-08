package cit.edu.primera.core.util

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun FirebaseAuth.currentUserIdFlow(): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser?.uid)
    }
    addAuthStateListener(listener)
    trySend(currentUser?.uid)
    awaitClose { removeAuthStateListener(listener) }
}
