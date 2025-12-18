package com.ssafy.chocopick.data.source.firebase.realtime

object RealtimePaths {
    const val USERS = "users"
    const val STORES = "stores"
    const val ORDERS = "orders"
    const val USER_ORDERS = "userOrders"   // 인덱스: userOrders/{uid}/{orderId}=true
    const val REWARDS = "rewards"          // rewards/{uid} = Reward
    const val COUPONS = "coupons"          // coupons/{uid}/{couponId} = Coupon
    const val REVIEWS = "reviews"
    const val PRODUCTS = "products"
}
