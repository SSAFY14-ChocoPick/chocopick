package com.ssafy.chocopick.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GmsApiProvider {

    private const val BASE_URL = "https://gms.ssafy.io/gmsapi/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val gemini: GmsGeminiApi = retrofit.create(GmsGeminiApi::class.java)

    const val GEMINI_25_FLASH_URL =
        "generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
}
