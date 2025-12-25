package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Reward

interface RewardRepository {
    suspend fun getReward(uid: String): Reward?
    suspend fun upsertReward(reward: Reward)

    suspend fun issueAmericanoIfPossible(uid: String): Reward
    suspend fun useAmericanoIfPossible(uid: String): Reward

    // ✅ 변경: stampAdd 추가
    suspend fun applyRewardForOrderIfNeeded(uid: String, orderId: String, stampAdd: Int): Reward
}
