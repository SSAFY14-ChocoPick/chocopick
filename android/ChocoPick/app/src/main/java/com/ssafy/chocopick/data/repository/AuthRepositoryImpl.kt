package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource

class AuthRepositoryImpl(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {
    override fun getCurrentUid(): String? = dataSource.currentUser()?.uid

    override suspend fun signUp(email: String, password: String): String {
        val user = dataSource.signUp(email, password)
        return user.uid
    }

    override suspend fun login(email: String, password: String): String {
        val user = dataSource.login(email, password)
        return user.uid
    }

    override suspend fun logout() = dataSource.logout()
}