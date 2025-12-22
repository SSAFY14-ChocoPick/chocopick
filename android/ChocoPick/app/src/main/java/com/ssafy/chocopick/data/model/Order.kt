package com.ssafy.chocopick.data.model

data class Order(
    val orderId: String = "",          // Firebase key
    val items: List<OrderItem> = emptyList(),
    val orderDate: Long = 0L,
    val status: String = "",           // "주문 완료"
    val store: String = "",            // storeId (ex. "store_006")
    val totalPrice: Int = 0,
    val uid: String = ""
)
