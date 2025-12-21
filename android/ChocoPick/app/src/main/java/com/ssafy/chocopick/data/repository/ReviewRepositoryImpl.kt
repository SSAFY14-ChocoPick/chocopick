package com.ssafy.chocopick.data.repository

import android.util.Log
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.data.model.ReviewStats
import com.ssafy.chocopick.data.source.firebase.realtime.ReviewDataSource

class ReviewRepositoryImpl(
    private val ds: ReviewDataSource
) : ReviewRepository {

    override suspend fun getReviews(productId: String, limit: Int): List<Review> =
        ds.getReviews(productId, limit)

    override suspend fun getMyReview(productId: String, uid: String): Review? =
        ds.getMyReview(productId, uid)

    override suspend fun upsertReview(review: Review): Review {
        Log.d("TAG", "fun upsert")
        val review = ds.upsertReview(review)
        return review
    }

    override suspend fun deleteReview(productId: String, reviewId: String, uid: String) =
        ds.deleteReview(productId, reviewId, uid)

    override suspend fun getStats(productId: String): ReviewStats? =
        ds.getStats(productId)
}