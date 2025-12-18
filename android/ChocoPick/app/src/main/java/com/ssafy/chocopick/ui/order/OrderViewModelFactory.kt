package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.data.repository.ProductRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.ProductDataSource

class OrderViewModelFactory : ViewModelProvider.Factory {
    private val repo : ProductRepository by lazy {
        ProductRepositoryImpl(ProductDataSource())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            return OrderViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}