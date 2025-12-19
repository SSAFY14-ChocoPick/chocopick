package com.ssafy.chocopick.data.source.firebase.realtime

import com.ssafy.chocopick.data.model.Reward

class RewardDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    suspend fun upsertReward(reward: Reward) {
        db.set("${RealtimePaths.REWARDS}/${reward.uid}", reward)
    }

    suspend fun getReward(uid: String): Reward? {
        val snap = db.get("${RealtimePaths.REWARDS}/$uid")
        return snap.getValue(Reward::class.java)
    }

    suspend fun updateStamps(uid: String, stamps: Int) {
        db.update(mapOf("${RealtimePaths.REWARDS}/$uid/stamps" to stamps))
    }
}