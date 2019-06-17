package com.learnteachcenter.ltcreikiclockv3.api

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.api.responses.*
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.User
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpResponse
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import retrofit2.Call
import retrofit2.http.*

interface ReikiApi {

    // Sign Up
    @FormUrlEncoded
    @POST("users/register")
    fun signUp(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password2") password2: String
    ): Call<SignUpResponse>

    // Log In
    @FormUrlEncoded
    @POST("users/login")
    fun logIn(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // Get
    @GET("reikis")
    fun getReikis(): LiveData<ApiResponse<ReikisResponse>>

    // Post Reiki
    @FormUrlEncoded
    @POST("reikis")
    fun addReiki(
        @Field("title") title: String,
        @Field("description") description: String?,
        @Field("playMusic") playMusic: Boolean,
        @Field("positions") positions: List<Position>?
    ): Call<AddReikiResponse>

    // Update Reiki
    @FormUrlEncoded
    @PUT("reikis/{id}")
    fun updateReiki(
        @Path("id") id: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("playMusic") playMusic: Boolean
    ): Call<UpdateReikiResponse>

    // Delete Reiki
    @DELETE("reikis/{id}")
    fun deleteReiki(
        @Path("id") reikiId: String
    ): Call<DeleteReikiResponse>
}