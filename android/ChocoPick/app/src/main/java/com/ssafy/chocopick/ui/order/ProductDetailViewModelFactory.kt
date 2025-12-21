package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.data.repository.ProductRepositoryImpl
import com.ssafy.chocopick.data.repository.ReviewRepository
import com.ssafy.chocopick.data.repository.ReviewRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.ProductDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.ReviewDataSource

class ProductDetailViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val productDataSource = ProductDataSource()
        val productRepo : ProductRepository = ProductRepositoryImpl(productDataSource)

        val reviewDataSource = ReviewDataSource()
        val reviewRepo: ReviewRepository = ReviewRepositoryImpl(reviewDataSource)
        return ProductDetailViewModel(productRepo, reviewRepo) as T
    }
}