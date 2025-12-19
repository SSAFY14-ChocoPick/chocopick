package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Store

data class StoreWithId(
    val storeId: String,
    val store: Store
)

class StoreDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    // /stores 아래 모든 매장 가져오기
    suspend fun getAllStores(): List<StoreWithId> {
        val snap = db.get(RealtimePaths.STORES) // "stores"
        val result = mutableListOf<StoreWithId>()

        for (child in snap.children) {
            val id = child.key ?: continue
            val store = child.getValue(Store::class.java) ?: continue
            result.add(StoreWithId(id, store))
        }

        // id 정렬(선택)
        return result.sortedBy { it.storeId }
    }

    // /stores/{storeId} 단일 매장
    suspend fun getStore(storeId: String): Store? {
        val snap = db.get("${RealtimePaths.STORES}/$storeId")
        return snap.getValue(Store::class.java)
    }
}
