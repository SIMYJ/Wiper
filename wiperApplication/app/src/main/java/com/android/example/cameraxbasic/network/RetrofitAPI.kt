package com.sample.gallery_outpup.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

//api 인터페이스 정의
interface RetrofitAPI {

    @GET("todos")
    fun request_get(): Call<String>

    @Multipart
    @POST("todos")//서버에 POST요청을 할 주소를 입력
    fun image_Request(
        //@Part("userId") userId: String,
        /** Call에 넘겨주는 ResponseBody는 okhttp3패키지의 ResponseBody이다. */
        //@Part file : MultipartBody.Part): Call<MultipartBody>
        //@Part file : MultipartBody.Part): Call<String>
        @Part file : MultipartBody.Part): Call<ResponseBody>
        //@Part file : MultipartBody.Part): Call<File>


    @Multipart
    @POST("todos")//서버에 POST요청을 할 주소를 입력
    fun image_Request02(
        /** Call에 넘겨주는 ResponseBody는 okhttp3패키지의 ResponseBody이다. */
        @Part file01 : MultipartBody.Part,
        @Part file02 : MultipartBody.Part): Call<ResponseBody>


    @POST("todos")
    fun request_post(): Call<String>


}
