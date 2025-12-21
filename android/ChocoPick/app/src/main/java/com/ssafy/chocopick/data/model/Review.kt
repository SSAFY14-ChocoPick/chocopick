package com.ssafy.chocopick.data.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val reviewId: String = "",
    val productId: String = "",
    val uid: String = "",
    val nickname: String = "",
    val rating: Float = 0.0F,
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) : Parcelable