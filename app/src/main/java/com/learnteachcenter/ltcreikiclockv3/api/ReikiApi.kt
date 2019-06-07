package com.learnteachcenter.ltcreikiclockv3.api

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.User
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpResponse
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
    ): Call<SignUpResponse>

    @FormUrlEncoded
    @POST("users/login")
    fun logIn(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("reikis")
    fun getReikis(): LiveData<ApiResponse<ReikisResponse>>
}