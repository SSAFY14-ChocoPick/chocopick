package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.model.User

class UserDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun createUserWithReward(user: User, reward: Reward) {
        // 단일 entity 전체 교체에는 set()이 적합함
//        db.set("${RealtimePaths.USERS}/${user.uid}", user)
        db.update(
            mapOf(
                "${RealtimePaths.USERS}/${user.uid}" to user,
                "${RealtimePaths.REWARDS}/${user.uid}" to reward
            )
        )
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