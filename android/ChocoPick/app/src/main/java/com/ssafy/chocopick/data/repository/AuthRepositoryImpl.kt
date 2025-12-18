package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.User
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class AuthRepositoryImpl(
    private val authDataSource: FirebaseAuthDataSource,
    private val userDataSource: UserDataSource
) : AuthRepository {
    override fun getCurrentUid(): String? = authDataSource.currentUser()?.uid

    override suspend fun signUp(email: String, password: String, nickname: String): String {
        // firebaseAuth 계정 생성
        val fbUser = authDataSource.signUp(email, password)

        // realtime DB에 저장
        val user = User(
            uid = fbUser.uid,
            email = email,
            nickname = nickname
        )

        try {
            userDataSource.upsertUser(user)
            android.util.Log.d("RTDB", "✅ user saved to RTDB: uid=${fbUser.uid}")
        } catch (e: Exception) {
            android.util.Log.e("RTDB", "❌ user save failed: ${e.message}", e)
            throw e
        }
        return fbUser.uid
    }

    override suspend fun login(email: String, password: String): String {
        val user = authDataSource.login(email, password)
        return user.uid
    }

    override suspend fun logout() = authDataSource.logout()
}