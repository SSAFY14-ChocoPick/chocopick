package com.ssafy.chocopick.data.source.firebase.realtime

import android.util.Log
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.data.model.ReviewStats
import kotlinx.coroutines.tasks.await
import kotlin.math.round

private const val TAG = "ReviewDataSource"
class ReviewDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {

    private fun reviewsPath(productId: String) = "${RealtimePaths.REVIEWS}/$productId"
    private fun reviewPath(productId: String, reviewId: String) = "${RealtimePaths.REVIEWS}/$productId/$reviewId"
    private fun statsPath(productId: String) = "${RealtimePaths.PRODUCT_REVIEW_STATS}/$productId"

    private fun normalizeRating(rating: Float): Float {
        val clamped = rating.coerceIn(0.0f, 5.0f)
        val halfStep = round(clamped * 2f) / 2f
        return halfStep
    }

    suspend fun getReviews(productId: String, limit: Int): List<Review> {
        val snap = db.child(RealtimePaths.REVIEWS)
            .child(productId)
            .orderByChild("createdAt")
            .limitToLast(limit)
            .get()
            .await()

        val list = snap.children.mapNotNull { child ->
            val r = child.getValue(Review::class.java) ?: return@mapNotNull null
            if (r.reviewId.isBlank()) r.copy(reviewId = child.key.orEmpty()) else r
        }

        return list.sortedByDescending { it.createdAt }
    }

    suspend fun getMyReview(productId: String, uid: String): Review? {
        val list = getReviews(productId, limit = 200)
        return list.firstOrNull { it.uid == uid }
    }

    /**
     * ✅ 여러 개 작성 허용:
     * - input.reviewId가 비어있으면 항상 "새 리뷰"로 pushKey 생성해서 저장
     * - 수정은 reviewId가 있는 경우에만 가능
     */
    suspend fun upsertReview(input: Review): Review {
        require(input.productId.isNotBlank()) { "productId is blank" }
        require(input.uid.isNotBlank()) { "uid is blank" }
        require(input.nickname.isNotBlank()) { "nickname is blank" }

        val now = System.currentTimeMillis()
        val fixedRating: Float = normalizeRating(input.rating)

        val isNew = input.reviewId.isBlank()
        val reviewId = if (isNew) db.pushKey(reviewsPath(input.productId)) else input.reviewId

        val before: Review? = if (isNew) null
        else db.get(reviewPath(input.productId, reviewId)).getValue(Review::class.java)

        val review = input.copy(
            reviewId = reviewId,
            rating = fixedRating,
            createdAt = if (isNew) now else (before?.createdAt ?: input.createdAt).takeIf { it > 0L } ?: now,
            updatedAt = now
        )

        val curStats = db.get(statsPath(review.productId))
            .getValue(ReviewStats::class.java)
            ?: ReviewStats(productId = review.productId)

        val newStats = if (before == null) {
            val newSum = curStats.ratingSum + review.rating
            val newCnt = curStats.reviewCount + 1
            curStats.copy(
                ratingSum = newSum,
                reviewCount = newCnt,
                avgRating = if (newCnt == 0) 0.0 else newSum / newCnt
            )
        } else {
            val newSum = curStats.ratingSum - before.rating + review.rating
            val newCnt = curStats.reviewCount.coerceAtLeast(1)
            curStats.copy(
                ratingSum = newSum,
                reviewCount = newCnt,
                avgRating = if (newCnt == 0) 0.0 else newSum / newCnt
            )
        }

        db.update(
            mapOf(
                reviewPath(review.productId, reviewId) to review,
                statsPath(review.productId) to newStats
            )
        )

        return review
    }

    suspend fun deleteReview(productId: String, reviewId: String, uid: String) {
        require(productId.isNotBlank()) { "productId is blank" }
        require(reviewId.isNotBlank()) { "reviewId is blank" }

        val beforeSnap = db.get(reviewPath(productId, reviewId))
        val before = beforeSnap.getValue(Review::class.java) ?: return

        if (before.uid != uid) {
            throw IllegalAccessException("본인 리뷰만 삭제할 수 있습니다.")
        }

        val statsSnap = db.get(statsPath(productId))
        val curStats = statsSnap.getValue(ReviewStats::class.java)
            ?: ReviewStats(productId = productId)

        val newCnt = (curStats.reviewCount - 1).coerceAtLeast(0)
        val newSum = (curStats.ratingSum - before.rating).coerceAtLeast(0.0)
        val newAvg = if (newCnt == 0) 0.0 else newSum / newCnt

        val updates = mapOf(
            reviewPath(productId, reviewId) to null,
            statsPath(productId) to curStats.copy(
                ratingSum = newSum,
                reviewCount = newCnt,
                avgRating = newAvg
            )
        )

        db.update(updates)
    }

    suspend fun getStats(productId: String): ReviewStats? {
        val snap = db.get(statsPath(productId))
        return snap.getValue(ReviewStats::class.java)
    }
}