package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Reward

interface RewardRepository {
    suspend fun getReward(uid: String): Reward?
}