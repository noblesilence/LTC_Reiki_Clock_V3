package com.learnteachcenter.ltcreikiclockv3.model.datasources.remote

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.model.authentication.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.model.reikis.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.authentication.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ReikiApi {

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
    fun getReikis(): LiveData<ApiResponse<ReikisResponse>>

//    @GET("reikis/sample")
//    fun getSampleReikis(): Call<List<Reiki>>
}