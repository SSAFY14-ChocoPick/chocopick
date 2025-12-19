package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.StoreWithId

class StoreRepositoryImpl(
    private val storeDataSource: StoreDataSource
) : StoreRepository {
    override suspend fun getAllStores(): List<StoreWithId> {
        return storeDataSource.getAllStores()
    }
}
