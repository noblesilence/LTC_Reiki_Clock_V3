package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository

class AllPositionsViewModel (
    private val repository: ReikiRepository = Injection.provideRepository(),
    private val reikiId: String) : ViewModel() {

    val positionsObservable: LiveData<List<Position>> = repository.getPositions(reikiId)

    fun getPositions(reikiId: String) = repository.getPositions(reikiId)

    fun getReikisObservable(): LiveData<List<Position>> {
        return positionsObservable
    }
}