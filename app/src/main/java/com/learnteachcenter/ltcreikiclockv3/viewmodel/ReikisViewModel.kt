package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.*
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource

class ReikisViewModel (private val repository: ReikiRepository = Injection.provideRepository())
    : ViewModel() {

    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()
}