package com.ssafy.chocopick.data.model

data class Reward(
    val uid: String = "",
    val stamps: Int = 0,
    val membershipTier: String = "BRONZE",
    val totalOrders: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val americanoCoupons: Int = 0
)

