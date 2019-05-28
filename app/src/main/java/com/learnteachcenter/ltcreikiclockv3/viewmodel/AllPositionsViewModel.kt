package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.reikis.ReikiRepository

class AllPositionsViewModel (
//    private val repository: ReikiRepository = Injection.provideRepository(),
    private val reikiId: String) : ViewModel() {

//    val positionsObservable: LiveData<List<Position>> = repository.getPositions(reikiId)
//
//    fun getPositions(reikiId: String) = repository.getPositions(reikiId)
//
//    fun getReikisObservable(): LiveData<List<Position>> {
//        return positionsObservable
//    }
}