package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Order

interface OrderRepository {

    /** ✅ 주문 1건 저장/갱신 */
    suspend fun upsertOrder(order: Order)

    /** ✅ 내 주문 목록(최신순) */
    suspend fun getOrders(uid: String, limit: Int = 50): List<Order>

    /** ✅ 내 최근 주문 1개 */
    suspend fun getMostRecentOrder(uid: String): Order?

    /** ✅ 주문 상세 1개 */
    suspend fun getOrder(uid: String, orderId: String): Order?
}
