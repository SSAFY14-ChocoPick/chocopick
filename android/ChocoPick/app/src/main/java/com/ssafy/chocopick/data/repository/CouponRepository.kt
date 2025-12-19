package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Coupon

interface CouponRepository {
    suspend fun getCoupons(uid: String): List<Coupon>
    suspend fun useCoupon(uid: String, couponId: String)
}
