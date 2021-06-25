package com.sample.gallery_outpup.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    //The gson builder
    var gson : Gson =  GsonBuilder()
        .setLenient()
        .create()

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build();


    //Retrofit Client 생성
    val mRetrofit = Retrofit.Builder()
        .baseUrl("http://34.64.198.137:5000/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

}