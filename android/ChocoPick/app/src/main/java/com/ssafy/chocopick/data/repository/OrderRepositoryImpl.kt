package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.source.firebase.realtime.OrderDataSource

class OrderRepositoryImpl(
    private val orderDataSource: OrderDataSource
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
}
