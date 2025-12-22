package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.OrderWithStore
import com.ssafy.chocopick.data.source.firebase.realtime.OrderDataSource

class OrderRepositoryImpl(
    private val orderDataSource: OrderDataSource,
    private val storeRepository: StoreRepository   // ✅ 추가 주입
) : OrderRepository {

    override suspend fun upsertOrder(order: Order) {
        orderDataSource.upsertOrder(order)
    }

    override suspend fun getOrders(uid: String, limit: Int): List<Order> {
        return orderDataSource.getOrders(uid, limit)
    }

    override suspend fun getMostRecentOrder(uid: String): Order? {
        return orderDataSource.getMostRecentOrder(uid)
    }

    override suspend fun getOrder(uid: String, orderId: String): Order? {
        return orderDataSource.getOrder(uid, orderId)
    }

    override suspend fun getOrderFromAll(orderId: String): Order? {
        TODO("Not yet implemented")
    }

    override suspend fun getOrdersWithStore(uid: String, limit: Int): List<OrderWithStore> {
        val orders = orderDataSource.getOrders(uid, limit)

        // ✅ stores 전체를 한 번에 가져와 Map으로 캐싱 (N+1 방지)
        val storeMap = storeRepository.getAllStores().associateBy { it.storeId }

        return orders.map { o ->
            val storeName = storeMap[o.store]?.name ?: o.store
            OrderWithStore(order = o, storeName = storeName)
        }
    }

    override suspend fun getOrderWithStore(uid: String, orderId: String): OrderWithStore? {
        val order = orderDataSource.getOrder(uid, orderId) ?: return null
        val storeName = storeRepository.getStore(order.store)?.name ?: order.store
        return OrderWithStore(order, storeName)
    }

    override suspend fun getMostRecentOrderWithStore(uid: String): OrderWithStore? {
        val order = orderDataSource.getMostRecentOrder(uid) ?: return null
        val storeName = storeRepository.getStore(order.store)?.name ?: order.store
        return OrderWithStore(order = order, storeName = storeName)
    }
}
