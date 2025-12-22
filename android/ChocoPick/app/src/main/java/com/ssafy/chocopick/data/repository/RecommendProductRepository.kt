package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.RecommendProduct

interface RecommendProductRepository {
    suspend fun getTop4RecommendProducts(): List<RecommendProduct>
}
