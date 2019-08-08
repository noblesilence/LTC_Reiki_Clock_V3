package com.learnteachcenter.ltcreikiclockv3.authentication.login

import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository

class LoginViewModel (val repository: ReikiRepository = Injection.provideReikiRepository()) : ViewModel() {
    fun clearLocalDatabase() = repository.clearLocalDatabase()
}