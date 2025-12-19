package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Coupon

class CouponDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun getCoupons(uid: String): List<Coupon> {
        val snap = db.get("${RealtimePaths.COUPONS}/$uid")
        val result = mutableListOf<Coupon>()
        for (child in snap.children) {
            val c = child.getValue(Coupon::class.java) ?: continue
            // couponId는 key로도 저장 가능하면 여기서 보정 가능
            result.add(
                if (c.couponId.isBlank()) c.copy(couponId = child.key ?: "")
                else c
            )
        }
        return result
    }

    suspend fun useCoupon(uid: String, couponId: String) {
        // 방식 1) used=true로 업데이트
        db.update(mapOf("${RealtimePaths.COUPONS}/$uid/$couponId/used" to true))

        // 방식 2) 아예 삭제하고 싶으면 suggest:
        // db.delete("${RealtimePaths.COUPONS}/$uid/$couponId")  (delete 구현 필요)
    }
}
