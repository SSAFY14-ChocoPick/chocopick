package com.ssafy.chocopick.data.source.firebase.realtime

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeDbClient(
    private val ref: DatabaseReference = FirebaseDatabase
        .getInstance("https://chocopick-b0c91-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference
) {
    fun child(path: String): DatabaseReference = ref.child(path)

    suspend fun set(path: String, value: Any?) {
        ref.child(path).setValue(value).await()
    }

    suspend fun update(updates: Map<String, Any?>) {
        ref.updateChildren(updates).await()
    }

    suspend fun get(path: String): DataSnapshot {
        return ref.child(path).get().await()
    }

    suspend fun remove(path: String) {
        ref.child(path).removeValue().await()
    }

    fun pushKey(path: String): String {
        return ref.child(path).push().key
            ?: error("Failed to create push key at $path")
    }
}