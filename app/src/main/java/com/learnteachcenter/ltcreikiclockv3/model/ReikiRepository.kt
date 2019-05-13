package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.basic.Resource
import com.learnteachcenter.ltcreikiclockv3.model.basic.Status
import com.learnteachcenter.ltcreikiclockv3.model.basic.Status.*
import com.learnteachcenter.ltcreikiclockv3.model.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class ReikiRepository constructor(
    private val reikiDao: ReikiDao = Injection.provideReikiDao(),
    private val reikiApi: ReikiApi = Injection.provideReikiApi()
) {

    val reikisListObservable = MutableLiveData<Resource<List<Reiki>>>()

    // Create
    fun addReiki(reiki: Reiki) {
        reikiDao.insert(reiki)
    }

    // Read
    fun getReikis() {
        var loadingList: List<Reiki>? = null
        if (reikisListObservable.value != null) {
            loadingList = reikisListObservable.value!!.data
        }
        reikisListObservable.value = Resource.loading(loadingList)
        loadAllReikisFromDB()
        getReikisFromWeb()
    }

    // Update
    fun updateReiki(reiki: Reiki) {
        // TODO
    }

    // Delete
    fun deleteReiki(reiki: Reiki) {
        // TODO
    }

    fun deleteAllReikisInLocalDB() {
        println("deleteAllReikisInLocalDB")

        val deleteReikisTask = DeleteAllReikisInDBTask(this)
        deleteReikisTask.execute()
    }

    private fun getReikisFromWeb() {
        println("getReikisFromWeb")
        reikiApi.getSampleReikis().enqueue(object : Callback<List<Reiki>> {
            override fun onResponse(call: Call<List<Reiki>>, response: Response<List<Reiki>>) {
                if (response.isSuccessful) {
                    setReikisListObservableStatus(Status.SUCCESS, null)
                    addReikisToDB(response.body())
                } else {
                    // error case
                    setReikisListObservableStatus(Status.ERROR, response.code().toString())
                    when (response.code()) {
                        404 -> println("not found")
                        500 -> println("not logged in or server broken")
                        else -> println("unknown error")
                    }
                }
            }

            override fun onFailure(call: Call<List<Reiki>>, t: Throwable) {
                Log.d("Reiki", t.message)
                setReikisListObservableStatus(Status.ERROR, t.message)
            }
        })
    }

    private fun loadAllReikisFromDB() {
        println("loadAllReikisFromDB")

        val loadReikisTask: LoadAllReikisFromDBTask = LoadAllReikisFromDBTask(this)
        loadReikisTask.execute()
    }

    private fun addReikisToDB(items: List<Reiki>?) {
        println("addReikisToDB")

        val addReikisTask: AddReikisToDBTask = AddReikisToDBTask(this)
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
     * @param mRecipesList the data that need to be updated
     * @param message optional message for error
     */
    private fun setReikisListObservableData(reikis: List<Reiki>, message: String?) {
        println("setReikisListObservableData")
        var loadingStatus = Status.LOADING
        if (reikisListObservable.value != null) {
            loadingStatus = reikisListObservable.value!!.status
        }
        when (loadingStatus) {
            LOADING -> reikisListObservable.setValue(Resource.loading(reikis))
            ERROR -> reikisListObservable.setValue(Resource.error(message, reikis))
            SUCCESS -> reikisListObservable.setValue(Resource.success(reikis))
        }
    }

    /**
     * This method changes the observable's LiveData status without changing the data
     * @param status The new status of LiveData
     * @param message optional message for error
     */
    private fun setReikisListObservableStatus(status: Status, message: String?) {
        println("setReikisListObservableStatus")
        var loadingList: List<Reiki>? = null
        if (reikisListObservable.value != null) {
            loadingList = reikisListObservable.value!!.data
        }
        when (status) {
            ERROR -> reikisListObservable.value = Resource.error(message, loadingList)
            LOADING -> reikisListObservable.value = Resource.loading(loadingList)
            SUCCESS -> if (loadingList != null) {
                reikisListObservable.value = Resource.success(loadingList)
            }
        }
    }
}