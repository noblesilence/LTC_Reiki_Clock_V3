package com.learnteachcenter.ltcreikiclockv3.reiki.all

import android.arch.lifecycle.*
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository

class ReikisViewModel (repository: ReikiRepository = Injection.provideReikiRepository())
    : ViewModel() {

    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()
}