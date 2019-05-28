package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.*
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.Resource
import com.learnteachcenter.ltcreikiclockv3.model.reikis.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.reikis.ReikiRepository

class ReikisViewModel (repository: ReikiRepository = Injection.provideReikiRepository())
    : ViewModel() {

    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()
}