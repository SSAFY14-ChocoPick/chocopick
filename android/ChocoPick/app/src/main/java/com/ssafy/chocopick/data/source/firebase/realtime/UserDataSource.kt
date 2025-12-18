package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.User

class UserDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun upsertUser(user: User) {
        db.set("${RealtimePaths.USERS}/${user.uid}", user)
    }

    suspend fun getUser(uid: String): User? {
        val snap = db.get("${RealtimePaths.USERS}/$uid")
        return snap.getValue(User::class.java)
    }

    suspend fun updateNickname(uid: String, nickname: String) {
        db.update(
            mapOf(
                "${RealtimePaths.USERS}/$uid/nickname" to nickname
            )
        )
    }
}