package com.ssafy.chocopick.data.remote

import com.ssafy.chocopick.data.model.FcmRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("api/test/fcm/delayed")
    suspend fun sendDelayed(@Body req: FcmRequestDto): String
}