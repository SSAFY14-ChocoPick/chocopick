package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.*
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.CouponDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class CouponsStampsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponsStampsViewModel::class.java)) {

            // Auth
            val authDs = FirebaseAuthDataSource()
            val userDs = UserDataSource()
            val authRepo: AuthRepository = AuthRepositoryImpl(authDs, userDs)

            // Reward
            val rewardDs = RewardDataSource()
            val rewardRepo: RewardRepository = RewardRepositoryImpl(rewardDs)

            // Coupon
            val couponDs = CouponDataSource()
            val couponRepo: CouponRepository = CouponRepositoryImpl(couponDs)

            @Suppress("UNCHECKED_CAST")
            return CouponsStampsViewModel(authRepo, rewardRepo, couponRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
