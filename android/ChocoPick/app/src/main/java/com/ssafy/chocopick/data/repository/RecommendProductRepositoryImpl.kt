package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.RecommendProduct
import com.ssafy.chocopick.data.source.firebase.realtime.RecommendProductDataSource

class RecommendProductRepositoryImpl(
    private val recommendProductDataSource: RecommendProductDataSource
) : RecommendProductRepository {

    override suspend fun getTop4RecommendProducts(): List<RecommendProduct> {
        return recommendProductDataSource.getTop4()
    }
}
