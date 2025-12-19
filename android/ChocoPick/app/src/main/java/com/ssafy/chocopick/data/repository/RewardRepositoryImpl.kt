package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource

class RewardRepositoryImpl(
    private val rewardDataSource: RewardDataSource
) : RewardRepository {

    override suspend fun getReward(uid: String) = rewardDataSource.getReward(uid)
    override suspend fun upsertReward(reward: Reward) = rewardDataSource.upsertReward(reward)

    override suspend fun issueAmericanoIfPossible(uid: String) =
        rewardDataSource.issueAmericanoIfPossible(uid)

    override suspend fun useAmericanoIfPossible(uid: String) =
        rewardDataSource.useAmericanoIfPossible(uid)
}
