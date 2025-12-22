package com.ssafy.chocopick.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.RecommendProductRepository
import com.ssafy.chocopick.data.repository.RecommendProductRepositoryImpl
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.data.repository.RewardRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.RecommendProductDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource

class HomeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {

            // ✅ Recommend
            val recommendRepo: RecommendProductRepository =
                RecommendProductRepositoryImpl(RecommendProductDataSource())

            // ✅ Reward
            val rewardRepo: RewardRepository =
                RewardRepositoryImpl(RewardDataSource())

            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(recommendRepo, rewardRepo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
