package com.learnteachcenter.ltcreikiclockv3.repositories

import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.api.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.DeletePositionResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.DeleteReikisResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.util.NetworkBoundResource
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

object ReikiRepositoryImpl: ReikiRepository {

    private val TAG = "Reiki"

    private val reikiDao: ReikiDao = Injection.provideReikiDao()
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
    private val appExecutors: AppExecutors = Injection.provideAppExecutors()

    /*
    * Reiki CRUD
    * */

    // Insert 1 Reiki
    override fun addReiki(reiki: Reiki) {
        InsertReikiAsyncTask().execute(reiki)
    }

    private class InsertReikiAsyncTask : AsyncTask<Reiki, Void, Void>() {
        override fun doInBackground(vararg reikis: Reiki): Void? {
            reikiDao.insertReiki(reikis[0])
            return null
        }
    }

    // Get all Reikis
    override fun getReikis(): LiveData<Resource<List<Reiki>>> {
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

    // Get one Reiki from the database
    override fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions> {
        return reikiDao.getReiki(reikiId)
    }

    // Update the list of Reikis (from reordering operation)
    override fun updateReikis(vararg reikis: Reiki) {
        UpdateReikisAsyncTask().execute(*reikis)
    }

    private class UpdateReikisAsyncTask : AsyncTask<Reiki, Void, Int>() {
        override fun doInBackground(vararg reikis: Reiki): Int {
            return reikiDao.updateReikis(*reikis)
        }
    }

    // Delete one Reiki in local and remote databases
    override fun deleteReikis(vararg reikis: Reiki) {
        // Delete in local database
        DeleteReikisAsyncTask().execute(*reikis)

        // Delete on the remote database
        val reikiIds = ArrayList<String>()

        for(r in reikis) {
            reikiIds.add(r.id)
        }

        val call: Call<DeleteReikisResponse> = reikiApi.deleteReikis(reikiIds)

        call.enqueue(object: Callback<DeleteReikisResponse> {
            override fun onFailure(call: Call<DeleteReikisResponse>, t: Throwable) {
                Log.wtf("Reiki", "Delete error: ${t.message}")
            }

            override fun onResponse(call: Call<DeleteReikisResponse>, response: Response<DeleteReikisResponse>) {
                val deleteResponse: DeleteReikisResponse? = response.body()

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

    private class DeleteReikisAsyncTask : AsyncTask<Reiki, Void, Void>() {
        override fun doInBackground(vararg reikis: Reiki): Void? {
            reikiDao.deleteReikis(*reikis)
            return null
        }
    }

    /*
    * Positions CRUD
    * */

    // Insert 1 Position
    override fun insertPosition(position: Position) {
        InsertPositionAsyncTask(reikiDao, position).execute()
    }

    private class InsertPositionAsyncTask internal constructor(private val dao: ReikiDao, private val position: Position)
        : AsyncTask<Position, Void, Void>() {
        override fun doInBackground(vararg params: Position): Void? {
            dao.insertPosition(position)
            return null
        }
    }

    // Update 1 Position
    override fun updatePosition(position: Position) {
        UpdatePositionAsyncTask().execute(position)
    }

    // Update 1 Position async task
    private class UpdatePositionAsyncTask : AsyncTask<Position, Void, Void>() {
        override fun doInBackground(vararg position: Position): Void? {
            val count: Int = reikiDao.updatePosition(position[0])
            return null
        }
    }

    // Update a list of Positions (reorder operation)
    override fun updatePositions(vararg positions: Position) {
        UpdatePositionsAsyncTask().execute(*positions)
    }

    private class UpdatePositionsAsyncTask : AsyncTask<Position, Void, Int>() {
        override fun doInBackground(vararg positions: Position): Int {
            return reikiDao.updatePositions(*positions)
        }
    }

    // Delete 1 Position (local and remote)
    override fun deletePosition(reikiId: String, positionId: String) {
        // Delete in local database
        DeletePositionAsyncTask(reikiDao, reikiId, positionId).execute()

        // Delete on remote database
        val call: Call<DeletePositionResponse> = reikiApi.deletePosition(reikiId, positionId)

        call.enqueue(object: Callback<DeletePositionResponse> {
            override fun onFailure(call: Call<DeletePositionResponse>, t: Throwable) {
                Log.wtf("Reiki", "Delete error: ${t.message}")
            }

            override fun onResponse(call: Call<DeletePositionResponse>, response: Response<DeletePositionResponse>) {
                val deleteResponse: DeletePositionResponse? = response.body()

                if(deleteResponse != null) {
                    Log.wtf("Reiki", "Delete success")
                } else {
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    Log.wtf("Reiki", jObjError.toString())
                }
            }
        })
    }

    private class DeletePositionAsyncTask internal constructor(
        private val dao: ReikiDao,
        private val reikiId: String,
        private val positionId: String
    ) : AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg params: String?): Void? {
            val deletedCount: Int = dao.deletePosition(reikiId, positionId)
            Log.d("Reiki", "[ReikiRepository] Deleted count is ${deletedCount}")
            return null
        }
    }
}