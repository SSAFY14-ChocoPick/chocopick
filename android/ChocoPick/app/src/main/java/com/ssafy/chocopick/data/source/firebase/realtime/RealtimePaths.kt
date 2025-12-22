package com.ssafy.chocopick.data.source.firebase.realtime

object RealtimePaths {
    const val USERS = "users"
    const val STORES = "stores"
    const val FAVORITES = "favorites"

    // ✅ 주문 경로 (너의 DB 구조)
    const val ALL_ORDERS = "all_orders"              // /all_orders/{orderId} = Order
    const val ORDERS_EACH_USER = "orders_eachUser"   // /orders_eachUser/{uid}/{orderId} = Order

    const val REWARDS = "rewards"
    const val COUPONS = "coupons"
    const val REVIEWS = "reviews"
    const val PRODUCT_REVIEW_STATS = "reviewStats"
    const val PRODUCTS = "products"
    const val RECOMMEND_PRODUCT = "recommendProduct"
}
