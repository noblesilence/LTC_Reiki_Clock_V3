package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.basic.Resource

class AllReikisViewModel (private val repository: ReikiRepository = Injection.provideRepository()) : ViewModel() {

    val reikisListObservable: MediatorLiveData<Resource<List<Reiki>>> = MediatorLiveData<Resource<List<Reiki>>>()

    init {
        reikisListObservable.addSource(
            repository.reikisListObservable,
            object : Observer<Resource<List<Reiki>>> {
                override fun onChanged(reikis: Resource<List<Reiki>>?) {
                    reikisListObservable.value = reikis
                }
            })
    }

    fun getReikis() = repository.getReikis()

    fun getReikisListObservable(): LiveData<Resource<List<Reiki>>> {
        return reikisListObservable
    }

    fun deleteAllReikisInDB() {
        repository.deleteAllReikisInLocalDB()
    }
}