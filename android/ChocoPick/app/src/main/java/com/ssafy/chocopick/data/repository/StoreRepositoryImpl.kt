package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource

class StoreRepositoryImpl(
    private val ds: StoreDataSource
) : StoreRepository {
    override suspend fun getAllStores(): List<Store> = ds.getAllStores()
    override suspend fun getStore(storeId: String): Store? = ds.getStore(storeId)
}
