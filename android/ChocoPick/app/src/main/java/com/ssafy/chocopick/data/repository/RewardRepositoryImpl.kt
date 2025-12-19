package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource

class RewardRepositoryImpl(
    private val rewardDataSource: RewardDataSource
) : RewardRepository {
    override suspend fun getReward(uid: String): Reward? {
        return rewardDataSource.getReward(uid)
    }
}
