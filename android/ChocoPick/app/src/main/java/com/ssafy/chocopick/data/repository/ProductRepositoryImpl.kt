package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.source.firebase.realtime.ProductDataSource

class ProductRepositoryImpl(private val dataSource : ProductDataSource) : ProductRepository {
    override suspend fun fetchProducts(): List<Product> {
        return dataSource.fetchProduct()
    }

    override suspend fun fetchProductById(productId : String): Product {
        return dataSource.fetchProductById(productId)
    }
}