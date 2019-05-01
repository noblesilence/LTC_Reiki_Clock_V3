package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.LiveData

interface ReikiRepository {
    fun saveReiki(reiki: Reiki)
    fun getAllReikis(): LiveData<List<Reiki>>
    fun clearAllReikis()
}