package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.room.RoomRepository

class AllReikisViewModel (private val repository: ReikiRepository = RoomRepository()) : ViewModel() {

    private val allReikisLiveData = repository.getAllReikis()

    fun getAllReikisLiveData() = allReikisLiveData

    fun clearAllReikis() = repository.clearAllReikis()
}