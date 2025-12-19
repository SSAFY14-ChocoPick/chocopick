package com.ssafy.chocopick.data.source.firebase.realtime

class FavoriteDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun getFavoriteStoreIds(uid: String): Set<String> {
        val snap = db.get("${RealtimePaths.FAVORITES}/$uid")
        val ids = mutableSetOf<String>()

        for (child in snap.children) {
            val storeId = child.key ?: continue
            val value = child.getValue(Boolean::class.java) ?: true
            if (value) ids.add(storeId)
        }
        return ids
    }

    suspend fun setFavorite(uid: String, storeId: String, favorite: Boolean) {
        // true/false 모두 저장(원하면 false는 삭제로 바꿀 수도 있음)
        db.update(mapOf("${RealtimePaths.FAVORITES}/$uid/$storeId" to favorite))
    }
}
