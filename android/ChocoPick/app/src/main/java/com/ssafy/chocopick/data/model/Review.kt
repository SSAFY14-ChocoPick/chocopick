package com.ssafy.chocopick.data.model

data class Review(
    val reviewId: String = "",
    val productId: String = "",
    val uid: String = "",
    val nickname: String = "",
    val rating: Float = 0.0F,      // 0.0 ~ 5.0 (0.5 step)
    val content: String = "",
    val createdAt: Long = 0L,      // epoch millis
    val updatedAt: Long = 0L       // 수정 시 갱신
)