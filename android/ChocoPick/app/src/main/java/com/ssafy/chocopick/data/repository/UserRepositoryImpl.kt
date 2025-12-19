package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.User
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class UserRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserRepository {
    override suspend fun getUser(uid: String): User? = userDataSource.getUser(uid)
}
