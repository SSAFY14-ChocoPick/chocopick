package com.ssafy.chocopick.data.remote

import com.ssafy.chocopick.data.model.FcmRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

//interface FcmApi {
//    @POST("api/test/fcm/delayed")
//    suspend fun sendDelayed(@Body req: FcmRequestDto): String
//}

interface FcmApi {
    @POST("api/test/fcm/delayed")
    suspend fun sendDelayed(@Body req: FcmRequestDto): String

    // ✅ 매장 주문 전용(2개 순차)
    @POST("api/test/fcm/storeDelayed")
    suspend fun sendStoreDelayed(@Body req: FcmRequestDto): String
}