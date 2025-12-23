package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource

class StoreRepositoryImpl(
    private val storeDataSource: StoreDataSource
) : StoreRepository {
    override suspend fun getAllStores(): List<Store> = storeDataSource.getAllStores()
    override suspend fun getStore(storeId: String): Store? = storeDataSource.getStore(storeId)
}
