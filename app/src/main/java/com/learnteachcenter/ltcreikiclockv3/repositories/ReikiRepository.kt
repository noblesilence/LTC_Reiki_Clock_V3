package com.learnteachcenter.ltcreikiclockv3.repositories

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.api.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.AddReikiResponse
import com.learnteachcenter.ltcreikiclockv3.authentication.User
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.one.ReikiGenerator
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.util.NetworkBoundResource
import com.learnteachcenter.ltcreikiclockv3.util.Resource

// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

object ReikiRepository {

    private val TAG = "Reiki"

    private val reikiDao: ReikiDao = Injection.provideReikiDao()
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
    private val appExecutors: AppExecutors = Injection.provideAppExecutors()

    fun getReikis(): LiveData<Resource<List<Reiki>>> {
        return object : NetworkBoundResource<List<Reiki>, ReikisResponse>(appExecutors) {
            override fun saveNetworkCallResult(response: ReikisResponse) {
                Log.d(TAG, "saveNetworkCallResult")

                val reikis: List<Reiki>? = response.reikis

                reikis?.forEach {reiki ->
                    reikiDao.insertReiki(reiki)
                    reiki.positions?.forEach { pos ->
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

    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions> {
        return reikiDao.getReiki(reikiId)
    }

    fun deleteAllReikis() {
        DeleteAsyncTask(reikiDao).execute()
    }

    private class DeleteAsyncTask internal constructor(private val dao: ReikiDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            dao.deleteAllReikis()
            return null
        }
    }
}