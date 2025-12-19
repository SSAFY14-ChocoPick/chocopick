package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.data.repository.ProductRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.ProductDataSource

class ProductDetailViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataSource = ProductDataSource()
        val repo : ProductRepository = ProductRepositoryImpl(dataSource)
        return ProductDetailViewModel(repo) as T
    }
}