package com.ssafy.chocopick.data.model

data class Order(
    val orderId: String = "",
    val uid: String = "",
    val storeId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PAID", // CREATED | PAID | READY | PICKED_UP | CANCELED(너는 취소 불가라 안 써도 됨)
    val items: Map<String, OrderItem> = emptyMap(),
    val earnedStamps: Int = 0,
    val totalPrice: Int = 0
)

data class OrderItem(
    val qty: Int = 0,
    val unitPrice: Int = 0,
    val category: String = "DRINK" // DRINK | DESSERT
)
