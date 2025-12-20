package com.ssafy.chocopick.data.model

data class ReviewStats(
    val productId: String = "",
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val ratingSum: Double = 0.0   // avg 계산용(Realtime에서 집계)
)