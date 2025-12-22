package com.ssafy.chocopick.data.source.firebase.realtime

object RealtimePaths {
    const val USERS = "users"
    const val STORES = "stores"
    const val FAVORITES = "favorites"
    const val ORDERS = "orders"
    const val USER_ORDERS = "userOrders"   // 인덱스: userOrders/{uid}/{orderId}=true
    const val REWARDS = "rewards"          // rewards/{uid} = Reward
    const val COUPONS = "coupons"          // coupons/{uid}/{couponId} = Coupon
    const val REVIEWS = "reviews"              // /reviews/{productId}/{reviewId}
    const val PRODUCT_REVIEW_STATS = "reviewStats" // /reviewStats/{productId}
    const val PRODUCTS = "products"
    const val RECOMMEND_PRODUCT = "recommendProduct"

}
