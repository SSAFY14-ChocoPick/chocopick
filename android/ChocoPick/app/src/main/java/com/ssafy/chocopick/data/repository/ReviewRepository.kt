package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.data.model.ReviewStats

interface ReviewRepository {
    suspend fun getReviews(productId: String, limit: Int = 100): List<Review>
    suspend fun getMyReview(productId: String, uid: String): Review?
    suspend fun upsertReview(review: Review): Review
    suspend fun deleteReview(productId: String, reviewId: String, uid: String)

    suspend fun getStats(productId: String): ReviewStats?
}