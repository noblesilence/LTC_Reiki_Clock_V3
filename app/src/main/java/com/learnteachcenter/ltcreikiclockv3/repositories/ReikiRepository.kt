package com.learnteachcenter.ltcreikiclockv3.repositories

import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.api.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.DeleteReikiResponse
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.util.NetworkBoundResource
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun deleteReiki(reikiId: String) {
        // Delete in local database
        DeleteReikiAsyncTask(reikiDao, reikiId).execute()

        // Delete on the remote database
        val call: Call<DeleteReikiResponse> = reikiApi.deleteReiki(reikiId)

        call.enqueue(object: Callback<DeleteReikiResponse> {
            override fun onFailure(call: Call<DeleteReikiResponse>, t: Throwable) {
                Log.wtf("Reiki", "Delete error: ${t.message}")
            }

            override fun onResponse(call: Call<DeleteReikiResponse>, response: Response<DeleteReikiResponse>) {
                val deleteResponse: DeleteReikiResponse? = response.body()

                if(deleteResponse != null) {
                    if(deleteResponse.success) {
                        Log.wtf("Reiki", "Delete success!")
                    }
                } else {
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    Log.wtf("Reiki", jObjError.toString())
                }
            }
        })
    }

    fun updateReikis(vararg reikis: Reiki) {
        val i: Int = reikiDao.updateReikis(*reikis)
    }

    fun insertPosition(position: Position) {
        InsertAsyncTask(reikiDao, position).execute()
    }

    private class InsertAsyncTask internal constructor(private val dao: ReikiDao, private val position: Position) : AsyncTask<Position, Void, Void>() {
        override fun doInBackground(vararg params: Position): Void? {
            dao.insertPosition(position)
            return null
        }
    }

    private class DeleteAsyncTask internal constructor(private val dao: ReikiDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            dao.deleteAllReikis()
            return null
        }
    }

    private class DeleteReikiAsyncTask internal constructor(private val dao: ReikiDao, private val reikiId: String) : AsyncTask<String, Void, Void> () {
        override fun doInBackground(vararg params: String?): Void? {
            dao.deleteReiki(reikiId)
            return null
        }
    }
}