package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Coupon
import com.ssafy.chocopick.data.source.firebase.realtime.CouponDataSource

class CouponRepositoryImpl(
    private val ds: CouponDataSource
) : CouponRepository {
    override suspend fun getCoupons(uid: String): List<Coupon> = ds.getCoupons(uid)
    override suspend fun useCoupon(uid: String, couponId: String) = ds.useCoupon(uid, couponId)
}
