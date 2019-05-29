package com.learnteachcenter.ltcreikiclockv3.model.reikis

import android.arch.lifecycle.LiveData
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.datasources.local.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.app.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.*
import retrofit2.Call

// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

object ReikiRepository {

    private val TAG = "Reiki"

    private val reikiDao: ReikiDao = Injection.provideReikiDao()
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
    private val appExecutors: AppExecutors = Injection.provideAppExecutors()

    fun getReikis(): LiveData<Resource<List<Reiki>>> {
        return object : NetworkBoundResource<List<Reiki>, ReikisResponse>(
            appExecutors
        ) {
            override fun saveNetworkCallResult(response: ReikisResponse) {
                Log.d(TAG, "saveNetworkCallResult")

                val reikis: List<Reiki>? = response.reikis

                reikis?.forEach {reiki ->
                    reikiDao.insertReiki(reiki)
                    reiki.positions.forEach { pos ->
                        pos.reikiId = reiki.id
                        reikiDao.insertPosition(pos)
                    }
                }
            }

            override fun shouldFetch(data: List<Reiki>?): Boolean {
                return NetworkUtil.isConnected(Injection.provideContext())
            }

            override fun loadFromDatabase(): LiveData<List<Reiki>> {
                return reikiDao.getReikis()
            }

            override fun createNetworkCall(): LiveData<ApiResponse<ReikisResponse>> {
                return reikiApi.getReikis()
            }
        }.asLiveData()
    }

    // Create
    fun addReiki(reiki: Reiki) {
        reikiDao.insertReiki(reiki)
    }
}