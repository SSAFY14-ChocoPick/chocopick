package com.ssafy.chocopick.data.source.firebase.realtime

import com.google.firebase.database.FirebaseDatabase
import com.ssafy.chocopick.data.model.Order
import kotlinx.coroutines.tasks.await

class OrderDataSource(
    private val rtdb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    private fun ordersRef(uid: String) =
        rtdb.reference.child(RealtimePaths.ORDERS).child(uid)

    /** /orders/{uid}/{orderId} = order */
    suspend fun upsertOrder(order: Order) {
        require(order.uid.isNotBlank()) { "order.uid is blank" }
        require(order.orderId.isNotBlank()) { "order.orderId is blank" }

        ordersRef(order.uid)
            .child(order.orderId)
            .setValue(order)
            .await()
    }

    /** 최신순 목록: createdAt 기준 limitToLast */
    suspend fun getOrders(uid: String, limit: Int): List<Order> {
        val snap = ordersRef(uid)
            .orderByChild("createdAt")
            .limitToLast(limit)
            .get()
            .await()

        // limitToLast는 "오래된→최신"으로 담기므로, 최신순으로 뒤집어줌
        val list = snap.children.mapNotNull { child ->
            val o = child.getValue(Order::class.java) ?: return@mapNotNull null
            if (o.orderId.isBlank()) o.copy(orderId = child.key.orEmpty()) else o
        }

        return list.sortedByDescending { it.createdAt }
    }

    /** 최근 1개 */
    suspend fun getMostRecentOrder(uid: String): Order? {
        val snap = ordersRef(uid)
            .orderByChild("createdAt")
            .limitToLast(1)
            .get()
            .await()

        val child = snap.children.firstOrNull() ?: return null
        val o = child.getValue(Order::class.java) ?: return null
        return if (o.orderId.isBlank()) o.copy(orderId = child.key.orEmpty()) else o
    }

    /** 상세 1개 */
    suspend fun getOrder(uid: String, orderId: String): Order? {
        val snap = ordersRef(uid)
            .child(orderId)
            .get()
            .await()

        val o = snap.getValue(Order::class.java) ?: return null
        return if (o.orderId.isBlank()) o.copy(orderId = orderId) else o
    }
}
