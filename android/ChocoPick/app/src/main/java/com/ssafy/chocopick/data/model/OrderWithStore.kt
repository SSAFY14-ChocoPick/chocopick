package com.ssafy.chocopick.data.model

// 주문(Order) 자체를 바꾸지 말고, 화면용 모델만 추가
data class OrderWithStore(
    val order: Order,
    val storeName: String = ""
) {
    val orderId get() = order.orderId
    val storeId get() = order.store
}
