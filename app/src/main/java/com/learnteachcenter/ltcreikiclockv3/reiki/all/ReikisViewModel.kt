package com.learnteachcenter.ltcreikiclockv3.reiki.all

import android.arch.lifecycle.*
import android.os.AsyncTask
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository

class ReikisViewModel (val repository: ReikiRepository = Injection.provideReikiRepository())
    : ViewModel() {

    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()

    // Update Reikis
    fun updateReikis(vararg reikis: Reiki) {
        val updateReikisTask = UpdateReikisTask(repository)
        updateReikisTask.execute(*reikis)
    }

    private class UpdateReikisTask internal constructor(private val repository: ReikiRepository)
        : AsyncTask<Reiki, Void, Void>() {
        override fun doInBackground(vararg reikis: Reiki): Void? {
            repository.updateReikis(*reikis)
            return null
        }
    }

    fun deleteReiki(reikiId: String) = repository.deleteReiki(reikiId)

    fun deleteReikis() = repository.deleteAllReikis()
}