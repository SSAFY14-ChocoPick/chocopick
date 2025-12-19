package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.source.firebase.realtime.FavoriteDataSource

class FavoriteRepositoryImpl(
    private val ds: FavoriteDataSource
) : FavoriteRepository {
    override suspend fun getFavoriteStoreIds(uid: String): Set<String> = ds.getFavoriteStoreIds(uid)
    override suspend fun setFavorite(uid: String, storeId: String, favorite: Boolean) =
        ds.setFavorite(uid, storeId, favorite)
}
