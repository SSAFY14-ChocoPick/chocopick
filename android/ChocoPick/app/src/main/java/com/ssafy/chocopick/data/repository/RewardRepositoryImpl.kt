package com.ssafy.chocopick.data.repository

import android.util.Log
import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource

private const val TAG = "RewardRepositoryImpl"

class RewardRepositoryImpl(
    private val rewardDataSource: RewardDataSource
) : RewardRepository {

    override suspend fun getReward(uid: String): Reward? {
        Log.d(TAG, "func getReward / in")
        val reward = rewardDataSource.getReward(uid)
        Log.d(TAG, "func getReward / out")
        return reward
    }

    override suspend fun upsertReward(reward: Reward) =
        rewardDataSource.upsertReward(reward)

    override suspend fun issueAmericanoIfPossible(uid: String) =
        rewardDataSource.issueAmericanoIfPossible(uid)

    override suspend fun useAmericanoIfPossible(uid: String) =
        rewardDataSource.useAmericanoIfPossible(uid)

    override suspend fun applyRewardForOrderIfNeeded(uid: String, orderId: String, stampAdd: Int): Reward {
        return rewardDataSource.applyRewardForOrderIfNeeded(uid, orderId, stampAdd)
    }
}
