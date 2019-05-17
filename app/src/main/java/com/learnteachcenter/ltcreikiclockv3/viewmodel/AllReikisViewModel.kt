package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource

class AllReikisViewModel (private val repository: ReikiRepository = Injection.provideRepository()) : ViewModel() {

    val reikisObservable: MediatorLiveData<Resource<List<Reiki>>> = MediatorLiveData<Resource<List<Reiki>>>()

    init {
        reikisObservable.addSource(
            repository.reikisObservable,
            object : Observer<Resource<List<Reiki>>> {
                override fun onChanged(reikis: Resource<List<Reiki>>?) {
                    reikisObservable.value = reikis
                }
            })
    }

    fun getReikis() = repository.getReikis()

    fun getReikisObservable(): LiveData<Resource<List<Reiki>>> {
        return reikisObservable
    }

    fun deleteAllReikisInDB() {
        repository.deleteAllReikisInLocalDB()
    }
}