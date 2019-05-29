package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.*
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.repository.ReikiRepository

class ReikisViewModel (repository: ReikiRepository = Injection.provideReikiRepository())
    : ViewModel() {

    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()
}