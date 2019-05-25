package com.learnteachcenter.ltcreikiclockv3.app

import android.app.Application
import android.content.Context
import com.learnteachcenter.ltcreikiclockv3.BuildConfig
import com.learnteachcenter.ltcreikiclockv3.model.authentication.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDao
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.learnteachcenter.ltcreikiclockv3.utils.AppExecutors


object Injection {
    fun provideRepository(): ReikiRepository = ReikiRepository()

    fun provideReikiDao(): ReikiDao = ReikiApplication.database.reikiDao()

    fun provideReikiApi(): ReikiApi {
        return provideRetrofit().create(ReikiApi::class.java)
    }

    fun provideAppExecutors() = AppExecutors()

    fun provideContext(): Context {
        val app: Application = ReikiApplication()
        return app.applicationContext
    }

    private fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(provideOkHttpClient())
            .build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()

        logging.level = if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }

    private fun provideOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideLoggingInterceptor())

        httpClient.addInterceptor { chain ->
            val request = chain.request().newBuilder().addHeader("Authorization", AuthenticationPrefs.getAuthToken()!!).build()
            chain.proceed(request)
        }

        return httpClient.build()
    }
}