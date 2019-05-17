package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.authentication.SessionManager
import com.learnteachcenter.ltcreikiclockv3.model.basic.Resource
import com.learnteachcenter.ltcreikiclockv3.model.basic.Status
import com.learnteachcenter.ltcreikiclockv3.model.basic.Status.*
import com.learnteachcenter.ltcreikiclockv3.model.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import android.widget.Toast
import android.R.string
import org.json.JSONObject



// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

class ReikiRepository constructor(
    private val reikiDao: ReikiDao = Injection.provideReikiDao(),
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
) {

    private val TAG = "Reiki"

    val registerUserObservable = MutableLiveData<Resource<User>>()

    val reikisObservable = MutableLiveData<Resource<List<Reiki>>>()

    fun registerUser(user: User) {
        reikiApi.registerUser(
            user.name,
            user.email,
            user.password,
            user.password2).enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    setRegisterUserObservableData(response.body()!!, "")
                    setRegisterUserObservableStatus(SUCCESS, "")
                    Log.d(TAG, "[ReikiRepository] Success registration")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setRegisterUserObservableStatus(ERROR,
                        response.code().toString() + " " + jObjError)

                    Log.d(TAG, "[ReikiRepository] Error message from the API: " + response.code().toString() + " " + jObjError)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, t.message)
                setRegisterUserObservableStatus(ERROR, t.message!!)
            }
        })
    }

    /**
     * This method changes the observable's LiveData data without changing the status
     * @param User the data that need to be updated
     * @param message optional message for error
     */
    fun setRegisterUserObservableData(user: User, message: String) {
        var loadingStatus = LOADING
        if (registerUserObservable.value != null) {
            loadingStatus = registerUserObservable.value!!.status
        }
        when (loadingStatus) {
            LOADING -> registerUserObservable.setValue(Resource.loading(user))
            SUCCESS -> registerUserObservable.setValue(Resource.success(user))
            ERROR -> registerUserObservable.setValue(Resource.error(user, message))
        }
    }

    /**
     * This method changes the observable's LiveData data without changing the status
     * @param user the data that need to be updated
     * @param message optional message for error
     */
    private fun setRegisterUserObservableStatus(status: Status, message: String) {

        var loadingUser: User? = null
        if(registerUserObservable.value != null) {
            loadingUser = registerUserObservable.value?.data
        }
        when(status) {
            LOADING -> registerUserObservable.value = Resource.loading(loadingUser)
            SUCCESS -> {
                if(loadingUser != null) {
                    registerUserObservable.value = Resource.success(loadingUser)
                }
            }
            ERROR -> registerUserObservable.value = Resource.error(loadingUser, message)
        }
    }

    // Create
    fun addReiki(reiki: Reiki) {
        reikiDao.insert(reiki)
    }

    // Read
    fun getReikis() {
        Log.d(TAG, "[ReikiRepository] getReikis")
        var loadingList: List<Reiki>? = null
        if (reikisObservable.value != null) {
            loadingList = reikisObservable.value!!.data
        }
        reikisObservable.value = Resource.loading(loadingList)
        loadAllReikisFromDB()
        getReikisFromWeb()
    }

    private fun getReikisFromWeb() {
        Log.d(TAG, "[ReikiRepository] getReikisFromWeb")
        reikiApi.getSampleReikis().enqueue(object : Callback<List<Reiki>> {
            override fun onResponse(call: Call<List<Reiki>>, response: Response<List<Reiki>>) {
                if (response.isSuccessful) {
                    setReikisListObservableStatus(SUCCESS, null)
                    addReikisToDB(response.body())
                } else {
                    // error case
                    setReikisListObservableStatus(ERROR, response.code().toString())
                    when (response.code()) {
                        404 -> println("not found")
                        500 -> println("server unavailable")
                        else -> println("unknown error")
                    }
                }
            }

            override fun onFailure(call: Call<List<Reiki>>, t: Throwable) {
                Log.d("Reiki", t.message)
                setReikisListObservableStatus(ERROR, t.message)
            }
        })
    }

    private fun loadAllReikisFromDB() {
        Log.d(TAG, "[ReikiRepository] loadAllReikisFromDB")

        val loadReikisTask = LoadAllReikisFromDBTask(this)
        loadReikisTask.execute()
    }

    private fun addReikisToDB(items: List<Reiki>?) {
        println("addReikisToDB")

        val addReikisTask = AddReikisToDBTask(this)
        addReikisTask.execute(items)
    }

    private class LoadAllReikisFromDBTask
        internal constructor(repo: ReikiRepository)
        : AsyncTask<Void, Void, List<Reiki>>() {

        private val repoReference: WeakReference<ReikiRepository> = WeakReference(repo)

        override fun doInBackground(vararg params: Void?): List<Reiki> {
            val repo = repoReference.get()
            return repo?.reikiDao!!.getAllReikis()
        }

        override fun onPostExecute(results: List<Reiki>?) {
            // check if there are data in the db
            if(results != null && results.isNotEmpty()) {
                val repo = repoReference.get()
                repo?.setReikisListObservableData(results, null)
            }
        }
    }

    private class AddReikisToDBTask
        internal constructor(repo: ReikiRepository)
        : AsyncTask<List<Reiki>, Void, Boolean>() {

        private val repoReference: WeakReference<ReikiRepository> = WeakReference(repo)

        override fun doInBackground(vararg reikis: List<Reiki>): Boolean? {

            val repo = repoReference.get()

            var needsUpdate = false
            for (item in reikis[0]) {
                //upsert implementation for future use
                val inserted = repo?.reikiDao!!.insert(item) //-1 if not inserted
                if (inserted == -1L) {
                    val updated = repo.reikiDao.update(item)
                    if (updated > 0) {
                        needsUpdate = true
                    }
                } else {
                    needsUpdate = true
                }

            }
            return needsUpdate
        }

        override fun onPostExecute(needUpdate: Boolean?) {
            if (needUpdate!!) {
                val repo = repoReference.get()

                repo?.loadAllReikisFromDB()
            }
        }
    }

    private class DeleteAllReikisInDBTask
        internal constructor(repo: ReikiRepository)
        : AsyncTask<Void, Void, Unit>() {

        private val repoReference: WeakReference<ReikiRepository> = WeakReference(repo)

        override fun doInBackground(vararg params: Void): Unit? {
            val repo = repoReference.get()
            return repo?.reikiDao!!.deleteAllReikis()
        }
    }

    /**
     * This method changes the observable's LiveData data without changing the status
     * @param reikis the data that need to be updated
     * @param message optional message for error
     */
    private fun setReikisListObservableData(reikis: List<Reiki>, message: String?) {
        Log.d(TAG, "[ReikiRepository] setReikisListObservableData")
        var loadingStatus = LOADING
        if (reikisObservable.value != null) {
            loadingStatus = reikisObservable.value!!.status
        }
        when (loadingStatus) {
            LOADING -> reikisObservable.setValue(Resource.loading(reikis))
            ERROR -> reikisObservable.setValue(Resource.error(reikis, message))
            SUCCESS -> reikisObservable.setValue(Resource.success(reikis))
        }
    }

    /**
     * This method changes the observable's LiveData status without changing the data
     * @param status The new status of LiveData
     * @param message optional message for error
     */
    private fun setReikisListObservableStatus(status: Status, message: String?) {
        Log.d(TAG, "[ReikiRepository] setReikisListObservableStatus")
        var loadingList: List<Reiki>? = null
        if (reikisObservable.value != null) {
            loadingList = reikisObservable.value!!.data
        }
        when (status) {
            ERROR -> reikisObservable.value = Resource.error(loadingList, message)
            LOADING -> reikisObservable.value = Resource.loading(loadingList)
            SUCCESS -> if (loadingList != null) {
                reikisObservable.value = Resource.success(loadingList)
            }
        }
    }

    /* Function for testing only. Remove later */
    fun deleteAllReikisInLocalDB() {
        println("deleteAllReikisInLocalDB")

        val deleteReikisTask = DeleteAllReikisInDBTask(this)
        deleteReikisTask.execute()
    }
}