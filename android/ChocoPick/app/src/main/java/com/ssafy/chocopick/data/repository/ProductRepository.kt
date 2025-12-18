package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Product

interface ProductRepository {
    suspend fun fetchProducts(): List<Product>
}