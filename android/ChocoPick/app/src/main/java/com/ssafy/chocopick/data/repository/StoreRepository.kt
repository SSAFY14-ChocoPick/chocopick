package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.source.firebase.realtime.StoreWithId

interface StoreRepository {
    suspend fun getAllStores(): List<StoreWithId>
}
