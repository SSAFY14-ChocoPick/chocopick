package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.model.RecommendProduct
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RecommendProductDataSource(
    private val client: RealtimeDbClient = RealtimeDbClient()
) {

    /**
     * ✅ /recommendProduct 에서 추천 productId 목록(최대 4개)을 가져온다.
     * - Map 형태: {0:"id",1:"id"...}
     * - List 형태: ["id1","id2"...]
     */
    private suspend fun getRecommendIds(limit: Int = 4): List<String> {
        val snap = client.get(RealtimePaths.RECOMMEND_PRODUCT)
        val value = snap.value ?: return emptyList()

        val ids = when (value) {
            is List<*> -> value.mapNotNull { it as? String }
            is Map<*, *> -> value.entries
                .sortedBy { (it.key as? String)?.toIntOrNull() ?: Int.MAX_VALUE }
                .mapNotNull { it.value as? String }
            else -> emptyList()
        }

        return ids.take(limit)
    }

    /**
     * ✅ /products/{productId} 읽어서 RecommendProduct로 변환
     */
    private suspend fun getRecommendProduct(productId: String): RecommendProduct? {
        val snap = client.get("${RealtimePaths.PRODUCTS}/$productId")
        val p = snap.getValue(Product::class.java) ?: return null

        return RecommendProduct(
            productId = productId,
            name = p.name,
            imageUrl = p.imageUrl,
            price = p.price
        )
    }

    /**
     * ✅ 추천 4개를 병렬로 조회해서 반환
     */
    suspend fun getTop4(): List<RecommendProduct> = coroutineScope {
        val ids = getRecommendIds(limit = 4)
        if (ids.isEmpty()) return@coroutineScope emptyList()

        val jobs = ids.map { id -> async { getRecommendProduct(id) } }
        jobs.mapNotNull { it.await() }.take(4)
    }
}
