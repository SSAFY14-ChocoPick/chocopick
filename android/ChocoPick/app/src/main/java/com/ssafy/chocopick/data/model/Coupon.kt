package com.ssafy.chocopick.data.model

data class Coupon(
    val couponId: String = "",
    val title: String = "",
    val expiresAt: Long = 0L,
    val used: Boolean = false
)
