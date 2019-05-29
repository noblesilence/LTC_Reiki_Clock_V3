package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.repository.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.util.Injection

class AllPositionsViewModel (reikiId: String) : ViewModel() {
    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var positions: LiveData<List<Position>> = repository.getPositions(reikiId)
}