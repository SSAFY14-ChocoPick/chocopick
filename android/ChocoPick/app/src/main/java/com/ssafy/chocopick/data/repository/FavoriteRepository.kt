package com.ssafy.chocopick.data.repository

interface FavoriteRepository {
    suspend fun getFavoriteStoreIds(uid: String): Set<String>
    suspend fun setFavorite(uid: String, storeId: String, favorite: Boolean)
}
