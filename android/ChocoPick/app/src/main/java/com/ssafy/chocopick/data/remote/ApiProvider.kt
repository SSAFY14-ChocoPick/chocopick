package com.ssafy.chocopick.data.remote

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ApiProvider {
    private const val BASE_URL = "http://192.168.33.124:8080/" // 에뮬레이터 -> PC localhost

    //192.168.33.123
//    10.0.2.2:8080
//    175.120.137.147
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