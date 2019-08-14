package com.learnteachcenter.ltcreikiclockv3.api

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.api.responses.*
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.AddPositionResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.DeletePositionResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.UpdatePositionResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.UpdatePositionsOrderResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.*
import com.learnteachcenter.ltcreikiclockv3.authentication.forgotpassword.ForgotPasswordResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpResponse
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.position.Position
import retrofit2.Call
import retrofit2.http.*

interface ReikiApi {

    /*
    * Authentication
    * */

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

    // Forgot password
    @FormUrlEncoded
    @POST("users/forgot-password")
    fun forgotPassword(
        @Field("email") email: String
    ): Call<ForgotPasswordResponse>

    /*
    * Reikis
    * */

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

    // Update Reikis order
    @PUT("reikis")
    fun updateReikisOrder(
        @Body reikis: List<Reiki>
    ): Call<UpdateReikisOrderResponse>

    // Update Positions order
    @PUT("reikis/{id}/positions")
    fun updatePositionsOrder(
        @Path("id") reikiId: String,
        @Body positions: List<Position>
    ): Call<UpdatePositionsOrderResponse>

    // Delete Reiki
    @HTTP(method = "DELETE", path = "reikis/delete", hasBody = true)
    fun deleteReikis(
        @Body reikis: ArrayList<String>
    ): Call<DeleteReikisResponse>

    /*
    * Positions
    * */

    @FormUrlEncoded
    @POST("reikis/position/{id}")
    fun addPosition(
        @Path("id") reikiId: String,
        @Field("title") title: String,
        @Field("duration") duration: String
    ): Call<AddPositionResponse>

    @FormUrlEncoded
    @PUT("reikis/{id}/positions/{position_id}")
    fun updatePosition(
        @Path("id") reikiId: String,
        @Path("position_id") positionId: String,
        @Field("title") title: String,
        @Field("duration") duration: String
    ): Call<UpdatePositionResponse>

    @HTTP(method="DELETE", path="reikis/{id}/positions/delete", hasBody = true)
    fun deletePositions(
        @Path("id") reikiId: String,
        @Body positions: ArrayList<String>
    ): Call<DeletePositionResponse>
}