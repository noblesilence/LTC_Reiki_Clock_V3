package com.learnteachcenter.ltcreikiclockv3.model.room

import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import com.learnteachcenter.ltcreikiclockv3.app.ReikiApplication
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository

class RoomRepository : ReikiRepository {

    private val reikiDao: ReikiDao = ReikiApplication.database.reikiDao()
    private val allReikis: LiveData<List<Reiki>>

    init {
        allReikis = reikiDao.getAllReikis()
    }

    override fun saveReiki(reiki: Reiki) {
        InsertAsyncTask(reikiDao).execute(reiki)
    }

    override fun getAllReikis(): LiveData<List<Reiki>> = allReikis

    override fun clearAllReikis() {
        val reikiArray = allReikis.value?.toTypedArray()
        if(reikiArray != null) {
            DeleteAsyncTask(reikiDao).execute(*reikiArray)
        }
    }

    private class InsertAsyncTask internal constructor(private val dao: ReikiDao)
        : AsyncTask<Reiki, Void, Void>() {
        override fun doInBackground(vararg params: Reiki): Void? {
            dao.insert(params[0])
            return null
        }
    }

    private class DeleteAsyncTask internal constructor(private val dao: ReikiDao)
        : AsyncTask<Reiki, Void, Void>() {
        override fun doInBackground(vararg params: Reiki): Void? {
            dao.clearReikis(*params)
            return null
        }
    }
}