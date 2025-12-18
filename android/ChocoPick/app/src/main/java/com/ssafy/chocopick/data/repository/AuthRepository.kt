package com.ssafy.chocopick.data.repository

import android.provider.ContactsContract

interface AuthRepository {
    fun getCurrentUid(): String?
    suspend fun signUp(email: String, password: String, nickname: String): String   // uid 반환
    suspend fun login(email: String, password: String): String    // uid 반환
    suspend fun logout()
}
