package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.AuthRepositoryImpl
import com.ssafy.chocopick.data.repository.CouponRepository
import com.ssafy.chocopick.data.repository.CouponRepositoryImpl
import com.ssafy.chocopick.data.repository.OrderRepository
import com.ssafy.chocopick.data.repository.OrderRepositoryImpl
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.data.repository.RewardRepositoryImpl
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.data.repository.StoreRepositoryImpl
import com.ssafy.chocopick.data.repository.UserRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.CouponDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.OrderDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class MyPageViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {

            // Auth
            val authDs = FirebaseAuthDataSource()
            val userDs = UserDataSource()
            val authRepo: AuthRepository = AuthRepositoryImpl(authDs, userDs)

            // User
            val userRepo = UserRepositoryImpl(userDs)

            // Reward
            val rewardDs = RewardDataSource()
            val rewardRepo: RewardRepository = RewardRepositoryImpl(rewardDs)

            // Store
            val storeDs = StoreDataSource()
            val storeRepo: StoreRepository = StoreRepositoryImpl(storeDs)

            // Order
            val orderDs = OrderDataSource()
            val orderRepo: OrderRepository = OrderRepositoryImpl(orderDs, storeRepo)

            // Coupon
            val couponDs = CouponDataSource()
            val couponRepo: CouponRepository = CouponRepositoryImpl(couponDs)


            @Suppress("UNCHECKED_CAST")
            return MyPageViewModel(authRepo, userRepo, rewardRepo, orderRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
