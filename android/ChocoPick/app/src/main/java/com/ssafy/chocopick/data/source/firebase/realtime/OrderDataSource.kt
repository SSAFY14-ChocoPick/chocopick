package com.ssafy.chocopick.data.source.firebase.realtime

import android.util.Log
import com.ssafy.chocopick.data.model.Order
import kotlinx.coroutines.tasks.await

private const val TAG = "OrderDataSource"

class OrderDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    private fun userOrdersRef(uid: String) =
        db.child(RealtimePaths.ORDERS_EACH_USER).child(uid)

    private fun allOrdersRef() =
        db.child(RealtimePaths.ALL_ORDERS)

    /**
     * ✅ /orders_eachUser/{uid}/{orderId} = order
     * ✅ /all_orders/{orderId} = order
     */
    suspend fun upsertOrder(order: Order) {
        require(order.uid.isNotBlank()) { "order.uid is blank" }
        require(order.orderId.isNotBlank()) { "order.orderId is blank" }

        val updates = mapOf(
            "${RealtimePaths.ORDERS_EACH_USER}/${order.uid}/${order.orderId}" to order,
            "${RealtimePaths.ALL_ORDERS}/${order.orderId}" to order
        )

        db.update(updates)
    }

    /** ✅ 사용자 주문 목록 (최신순) */
    suspend fun getOrders(uid: String, limit: Int): List<Order> {
        val snap = userOrdersRef(uid)
            .orderByChild("orderDate")
            .limitToLast(limit)
            .get()
            .await()

        val list = snap.children.mapNotNull { child ->
            val o = child.getValue(Order::class.java) ?: return@mapNotNull null
            // orderId가 비어있으면 key로 채워주기
            if (o.orderId.isBlank()) o.copy(orderId = child.key.orEmpty()) else o
        }

        return list.sortedByDescending { it.orderDate }
    }

    /** ✅ 최근 주문 1개 */
    suspend fun getMostRecentOrder(uid: String): Order? {
        Log.d(TAG, "getMostRecentOrder IN uid=$uid")

        return try {
            val query = userOrdersRef(uid)
                .orderByChild("orderDate")
                .limitToLast(1)

            val snap = query.get().await()

            Log.d(TAG, "snap.exists=${snap.exists()} childrenCount=${snap.childrenCount}")

            val child = snap.children.lastOrNull()
            if (child == null) {
                Log.d(TAG, "No child under /orders_eachUser/$uid")
                return null
            }

            Log.d(TAG, "child.key=${child.key}")

            val o = child.getValue(Order::class.java)
            if (o == null) {
                Log.d(TAG, "getValue(Order::class.java) returned null. raw=${child.value}")
                return null
            }

            Log.d(TAG, "getMostRecentOrder OUT o=$o")
            if (o.orderId.isBlank()) o.copy(orderId = child.key.orEmpty()) else o

        } catch (e: Exception) {
            Log.e(TAG, "getMostRecentOrder ERROR", e)
            null
        }
    }


    /** ✅ 사용자 주문 상세 1개 */
    suspend fun getOrder(uid: String, orderId: String): Order? {
        val snap = userOrdersRef(uid)
            .child(orderId)
            .get()
            .await()

        val o = snap.getValue(Order::class.java) ?: return null
        return if (o.orderId.isBlank()) o.copy(orderId = orderId) else o
    }

    /** ✅ 전체 주문 상세 1개 (관리/운영/디버그용) */
    suspend fun getOrderFromAll(orderId: String): Order? {
        val snap = allOrdersRef()
            .child(orderId)
            .get()
            .await()

        val o = snap.getValue(Order::class.java) ?: return null
        return if (o.orderId.isBlank()) o.copy(orderId = orderId) else o
    }
}
