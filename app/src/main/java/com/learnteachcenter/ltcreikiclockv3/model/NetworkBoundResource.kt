package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.learnteachcenter.ltcreikiclockv3.BuildConfig
import com.learnteachcenter.ltcreikiclockv3.model.remote.Error
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource
import com.learnteachcenter.ltcreikiclockv3.utils.AppExecutors
import retrofit2.Call
import java.io.IOException

abstract class NetworkBoundResource<T>(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<T>>()

    init {
        result.value = Resource.loading(null)
        val dbSource = loadFromDatabase()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if(shouldLoadFromNetwork(data)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData -> result.value = Resource.success(newData) }
            }
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<T>) {

        appExecutors.networkIO().execute {

            try {
                val response = createNetworkCall().execute()

                println("response is $response")

                when(response.isSuccessful) {
                    true -> appExecutors.diskIO().execute {
                        saveNetworkCallResult(response.body())

                        appExecutors.mainThread().execute {
                            val newDbSource = loadFromDatabase()
                            result.addSource(newDbSource) { newData ->
                                result.removeSource(newDbSource)
                                result.value = Resource.success(newData)
                            }
                        }
                    }

                    false -> appExecutors.mainThread().execute {
                        result.addSource(dbSource) { newData ->
                            result.value = Resource.error(newData, Error(response.code(), response.message()))
                        }
                    }
                }
            } catch (exc: IOException) {
                System.err.println("Make sure your server ${BuildConfig.URL} is running.")
                appExecutors.mainThread().execute {
                    result.addSource(dbSource) { newData ->
                        result.setValue(Resource.error(newData, Error(503, "Service Unavailable.")))
                    }
                }
            }
        }
    }

    fun asLiveData(): LiveData<Resource<T>> = result

    @WorkerThread
    protected abstract fun saveNetworkCallResult(data: T?)

    @MainThread
    protected abstract fun shouldLoadFromNetwork(data: T?): Boolean

    @MainThread
    protected abstract fun loadFromDatabase(): LiveData<T>

    @WorkerThread
    protected abstract fun createNetworkCall(): Call<T>
}