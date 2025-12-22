package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.OrderWithStore

interface OrderRepository {

    /** ✅ 주문 1건 저장/갱신 (orders_eachUser + all_orders 동시 저장) */
    suspend fun upsertOrder(order: Order)

    /** ✅ 내 주문 목록(최신순) */
    suspend fun getOrders(uid: String, limit: Int = 50): List<Order>

    /** ✅ 내 최근 주문 1개 */
    suspend fun getMostRecentOrder(uid: String): Order?

    /** ✅ 내 주문 상세 1개 */
    suspend fun getOrder(uid: String, orderId: String): Order?

    /** ✅ 전체 주문 상세 1개 (관리/운영/디버그용) */
    suspend fun getOrderFromAll(orderId: String): Order?

    // ✅ 추가: storeName 포함
    suspend fun getOrdersWithStore(uid: String, limit: Int = 50): List<OrderWithStore>
    suspend fun getMostRecentOrderWithStore(uid: String): OrderWithStore?
    suspend fun getOrderWithStore(uid: String, orderId: String): OrderWithStore?


}
