package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Store

class StoreDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun setStore(store: Store) {
        db.set("${RealtimePaths.STORES}/${store.storeId}", store)
    }

    suspend fun setStores(stores: List<Store>) {
        val updates = stores.associate { store ->
            "${RealtimePaths.STORES}/${store.storeId}" to store
        }
        db.update(updates)
    }

    suspend fun getStore(storeId: String): Store? {
        val snap = db.get("${RealtimePaths.STORES}/$storeId")
        return snap.getValue(Store::class.java)
    }

    suspend fun getAllStores(): List<Store> {
        val snap = db.get(RealtimePaths.STORES)
        return snap.children.mapNotNull { it.getValue(Store::class.java) }
    }
}
