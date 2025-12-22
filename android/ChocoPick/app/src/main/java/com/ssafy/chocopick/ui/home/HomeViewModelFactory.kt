package com.ssafy.chocopick.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.RecommendProductRepository
import com.ssafy.chocopick.data.repository.RecommendProductRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.RecommendProductDataSource

class HomeViewModelFactory(
    private val recommendProductRepository: RecommendProductRepository =
        RecommendProductRepositoryImpl(RecommendProductDataSource())
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(recommendProductRepository) as T
    }
}
