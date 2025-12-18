package com.ssafy.chocopick.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val nickname: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)