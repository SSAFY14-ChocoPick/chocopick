package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Reward

interface RewardRepository {
    suspend fun getReward(uid: String): Reward?
    suspend fun upsertReward(reward: Reward)
    suspend fun issueAmericanoIfPossible(uid: String): Reward

    // ✅ 추가: 아메리카노 쿠폰 1장 사용(감소)
    suspend fun useAmericanoIfPossible(uid: String): Reward
}
