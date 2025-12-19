package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Store

interface StoreRepository {
    suspend fun getAllStores(): List<Store>
    suspend fun getStore(storeId: String): Store?
}
