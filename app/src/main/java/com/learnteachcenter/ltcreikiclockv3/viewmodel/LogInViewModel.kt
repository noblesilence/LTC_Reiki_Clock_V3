package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.authentication.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource

class LogInViewModel (private val repository: ReikiRepository
                      = Injection.provideRepository()) : ViewModel() {
    val TAG = "Reiki"
    val loginObservable = MediatorLiveData<Resource<LoginResponse>> ()

    init {
        loginObservable.addSource(
            repository.logInObservable,
            object: Observer<Resource<LoginResponse>> {
                override fun onChanged(loginResponse: Resource<LoginResponse>?) {
                    Log.d(TAG, "[LogInViewModel] onChanged, success: $loginResponse?.success, token: $loginResponse?.token")
                    loginObservable.value = loginResponse
                }
            }
        )
    }

    fun logIn(email: String, password: String) = repository.logIn(email, password)

    fun getLogInObservable(): LiveData<Resource<LoginResponse>> {
        return loginObservable
    }
}