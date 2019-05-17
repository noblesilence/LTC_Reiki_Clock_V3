package com.learnteachcenter.ltcreikiclockv3.model.remote

import com.learnteachcenter.ltcreikiclockv3.model.authentication.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.authentication.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ReikiApi {
    @GET("reikis/sample")
    fun getSampleReikis(): Call<List<Reiki>>

    @FormUrlEncoded
    @POST("users/register")
    fun signUp(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password2") password2: String
    ): Call<User>

    @FormUrlEncoded
    @POST("users/login")
    fun logIn(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("reikis")
    fun getReikis(): Call<List<Reiki>>
}