package com.ssafy.chocopick.data.model

data class Coupon(
    val couponId: String = "",
    val type: String = "FREE_DRINK",      // 현재는 무료음료 쿠폰만
    val status: String = "ACTIVE",        // ACTIVE | USED | EXPIRED
    val issuedAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,
    val reason: String = "STAMP_10"
)
