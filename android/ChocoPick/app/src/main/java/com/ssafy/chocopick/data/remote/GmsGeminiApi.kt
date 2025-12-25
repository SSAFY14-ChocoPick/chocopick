package com.ssafy.chocopick.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GmsGeminiApi {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body body: GeminiGenerateRequest
    ): GeminiGenerateResponse
}
