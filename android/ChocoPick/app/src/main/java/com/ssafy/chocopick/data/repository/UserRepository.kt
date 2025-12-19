package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.User

interface UserRepository {
    suspend fun getUser(uid: String): User?
}
