package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Store

class StoreDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun getAllStores(): List<Store> {
        val snap = db.get(RealtimePaths.STORES)
        val result = mutableListOf<Store>()

        for (child in snap.children) {
            val storeId = child.key ?: continue
            val store = child.getValue(Store::class.java) ?: continue
            result.add(store.copy(storeId = storeId))
        }
        return result
    }

    suspend fun getStore(storeId: String): Store? {
        val snap = db.get("${RealtimePaths.STORES}/$storeId")
        val store = snap.getValue(Store::class.java)
        return store?.copy(storeId = storeId)
    }
}
