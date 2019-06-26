package com.learnteachcenter.ltcreikiclockv3.reiki.all

import android.arch.lifecycle.*
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository

class ReikisViewModel (val repository: ReikiRepository = Injection.provideReikiRepository()) : ViewModel() {
    var reikis: LiveData<Resource<List<Reiki>>> = repository.getReikis()
    fun updateReikis(vararg reikis: Reiki) = repository.updateReikis(*reikis)
    fun deleteReikis() = repository.deleteAllReikis()
    fun deleteReiki(reikiId: String) = repository.deleteReiki(reikiId)
}