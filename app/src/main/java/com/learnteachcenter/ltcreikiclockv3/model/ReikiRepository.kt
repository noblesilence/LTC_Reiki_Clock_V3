package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.authentication.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.model.authentication.User
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource
import com.learnteachcenter.ltcreikiclockv3.model.remote.Status
import com.learnteachcenter.ltcreikiclockv3.model.remote.Status.*
import com.learnteachcenter.ltcreikiclockv3.model.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import org.json.JSONObject



// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

class ReikiRepository constructor(
    private val reikiDao: ReikiDao = Injection.provideReikiDao(),
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
) {

    private val TAG = "Reiki"

    val signUpObservable = MutableLiveData<Resource<User>>()
    val logInObservable = MutableLiveData<Resource<LoginResponse>>()
    val reikisObservable = MutableLiveData<Resource<List<Reiki>>>()

    /**
     * SIGN UP
     */
    fun signUp(user: User) {
        reikiApi.signUp(
            user.name,
            user.email,
            user.password,
            user.password2).enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    setSignUpObservableData(response.body()!!, "")
                    setSignUpObservableStatus(SUCCESS, "")
                    Log.d(TAG, "[ReikiRepository] Success registration")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setSignUpObservableStatus(ERROR,
                        response.code().toString() + " " + jObjError)

                    Log.d(TAG, "[ReikiRepository] Error message from the API: " + response.code().toString() + " " + jObjError)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, t.message)
                setSignUpObservableStatus(ERROR, t.message!!)
            }
        })
    }

    fun setSignUpObservableData(user: User, message: String) {
        var loadingStatus = LOADING
        if (signUpObservable.value != null) {
            loadingStatus = signUpObservable.value!!.status
        }
        when (loadingStatus) {
            LOADING -> signUpObservable.setValue(Resource.loading(user))
            SUCCESS -> signUpObservable.setValue(Resource.success(user))
            ERROR -> signUpObservable.setValue(Resource.error(user, message))
        }
    }

    private fun setSignUpObservableStatus(status: Status, message: String) {

        var loadingUser: User? = null
        if(signUpObservable.value != null) {
            loadingUser = signUpObservable.value?.data
        }
        when(status) {
            LOADING -> signUpObservable.value = Resource.loading(loadingUser)
            SUCCESS -> {
                if(loadingUser != null) {
                    signUpObservable.value = Resource.success(loadingUser)
                }
            }
            ERROR -> signUpObservable.value = Resource.error(loadingUser, message)
        }
    }

    /**
     * LOGIN
     */
    fun logIn(email: String, password: String) {
        reikiApi.logIn(email, password).enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful) {
                    setLogInObservableData(response.body()!!, "")
                    setLogInObservableStatus(SUCCESS, "")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setLogInObservableStatus(ERROR, jObjError.toString())

                    Log.d(TAG,
                        "[ReikiRepository] Error logging in: Status $response.code().toString() $jObjError")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d(TAG, t.message)
                setLogInObservableStatus(ERROR, t.message!!)
            }
        })
    }

    fun setLogInObservableData(loginResponse: LoginResponse, message: String) {
        var loadingStatus = LOADING
        if(logInObservable.value != null) {
            loadingStatus = logInObservable.value!!.status
        }
        when(loadingStatus) {
            LOADING -> logInObservable.value = Resource.loading(loginResponse)
            SUCCESS -> logInObservable.value = Resource.success(loginResponse)
            ERROR -> logInObservable.value = Resource.error(loginResponse, message)
        }
    }

    fun setLogInObservableStatus(status: Status, message: String) {
        var loadingLoginResponse: LoginResponse? = null
        if(logInObservable.value != null) {
            loadingLoginResponse = logInObservable.value?.data
        }
        when(status) {
            LOADING -> logInObservable.value = Resource.loading(loadingLoginResponse)
            SUCCESS -> {
                if(loadingLoginResponse != null) {
                    logInObservable.value = Resource.success(loadingLoginResponse)
                }
            }
            ERROR -> logInObservable.value = Resource.error(loadingLoginResponse, message)
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
        reikiApi.getReikis().enqueue(object : Callback<List<Reiki>> {
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