package com.learnteachcenter.ltcreikiclockv3.api

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.api.responses.AddReikiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.DeleteReikiResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.User
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpResponse
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import retrofit2.Call
import retrofit2.http.*

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

    @FormUrlEncoded
    @POST("reikis")
    fun addReiki(
        @Field("title") title: String,
        @Field("description") description: String?,
        @Field("playMusic") playMusic: Boolean,
        @Field("positions") positions: List<Position>?
    ): Call<AddReikiResponse>

    @DELETE("reikis/{id}")
    fun deleteReiki(
        @Path("id") reikiId: String
    ): Call<DeleteReikiResponse>
}