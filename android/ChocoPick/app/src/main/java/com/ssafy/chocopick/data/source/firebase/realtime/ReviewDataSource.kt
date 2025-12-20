package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.data.model.ReviewStats
import kotlinx.coroutines.tasks.await
import kotlin.math.round

class ReviewDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {

    private fun reviewsPath(productId: String) = "${RealtimePaths.REVIEWS}/$productId"
    private fun reviewPath(productId: String, reviewId: String) = "${RealtimePaths.REVIEWS}/$productId/$reviewId"
    private fun statsPath(productId: String) = "${RealtimePaths.PRODUCT_REVIEW_STATS}/$productId"

    private fun normalizeRating(rating: Float): Double {
        // 0.5 단위로 반올림 + 범위 제한
        val clamped = rating.coerceIn(0.0F, 5.0F)
        val halfStep = round(clamped * 2) / 2.0
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
        // RealtimeDB는 복잡한 where가 약해서: 일단 limit로 가져와서 uid로 필터
        // (리뷰가 매우 많아지면 별도 인덱스(유저리뷰맵) 추천)
        val list = getReviews(productId, limit = 200)
        return list.firstOrNull { it.uid == uid }
    }

    suspend fun upsertReview(input: Review): Review {
        require(input.productId.isNotBlank()) { "productId is blank" }
        require(input.uid.isNotBlank()) { "uid is blank" }
        require(input.nickname.isNotBlank()) { "nickname is blank" }

        val now = System.currentTimeMillis()
        val rating: Float = normalizeRating(input.rating).toFloat()

        val isNew = input.reviewId.isBlank()
        val reviewId = if (isNew) db.pushKey(reviewsPath(input.productId)) else input.reviewId

        // 기존 리뷰가 있으면 stats 보정(수정/신규 구분 필요)
        val before = if (isNew) null else db.get(reviewPath(input.productId, reviewId))
            .getValue(Review::class.java)

        val review = input.copy(
            reviewId = reviewId,
            rating = rating,
            createdAt = if (isNew) now else input.createdAt.takeIf { it > 0 } ?: now,
            updatedAt = now
        )

        // ✅ 멀티 업데이트 (review + stats 동시 반영)
        val updates = mutableMapOf<String, Any?>(
            reviewPath(review.productId, reviewId) to review
        )

        // stats 계산
        val statsSnap = db.get(statsPath(review.productId))
        val curStats = statsSnap.getValue(ReviewStats::class.java) ?: ReviewStats(productId = review.productId)

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

        updates[statsPath(review.productId)] = newStats

        db.update(updates)
        return review
    }

    suspend fun deleteReview(productId: String, reviewId: String, uid: String) {
        require(productId.isNotBlank()) { "productId is blank" }
        require(reviewId.isNotBlank()) { "reviewId is blank" }

        val beforeSnap = db.get(reviewPath(productId, reviewId))
        val before = beforeSnap.getValue(Review::class.java) ?: return

        // ✅ 내 리뷰만 삭제 가능
        if (before.uid != uid) {
            throw IllegalAccessException("본인 리뷰만 삭제할 수 있습니다.")
        }

        val statsSnap = db.get(statsPath(productId))
        val curStats = statsSnap.getValue(ReviewStats::class.java) ?: ReviewStats(productId = productId)

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