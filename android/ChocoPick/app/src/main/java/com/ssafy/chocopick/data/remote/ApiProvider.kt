package com.ssafy.chocopick.data.remote

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ApiProvider {
    // 10.0.2.2 = 안드로이드 에뮬레이터에서 본 PC(localhost). 실기기 테스트 시 PC의 LAN IP로 변경.
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    val fcmApi: FcmApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // Add this first!
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FcmApi::class.java)
    }
}