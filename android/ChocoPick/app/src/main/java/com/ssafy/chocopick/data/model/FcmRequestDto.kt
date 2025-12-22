package com.ssafy.chocopick.data.model

data class FcmRequestDto (
    val token: String,
    val title: String? = null,
    val body: String? = null
)