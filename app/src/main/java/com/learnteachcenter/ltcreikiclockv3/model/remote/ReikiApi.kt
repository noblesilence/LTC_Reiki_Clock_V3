package com.learnteachcenter.ltcreikiclockv3.model.remote

import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import retrofit2.Call
import retrofit2.http.GET

interface ReikiApi {
    @GET("reikis/sample")
    fun getSampleReikis(): Call<List<Reiki>>
}