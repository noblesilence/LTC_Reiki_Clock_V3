package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.api.responses.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.repository.UserRepository
import com.learnteachcenter.ltcreikiclockv3.util.Resource

class LogInViewModel (private val repository: UserRepository
                      = Injection.provideUserRepository()) : ViewModel() {
    val TAG = "Reiki"
    val loginObservable = MediatorLiveData<Resource<LoginResponse>> ()

    init {
        loginObservable.addSource(
            repository.logInObservable,
            object: Observer<Resource<LoginResponse>> {
                override fun onChanged(loginResponse: Resource<LoginResponse>?) {
                    when (loginResponse?.status) {
                        Resource.Status.SUCCESS -> {
                            Log.d(TAG, "[LogInViewModel] Login response status is SUCCESS.")
                        }
                        Resource.Status.LOADING -> {
                            Log.d(TAG, "[LogInViewModel] Login response status is LOADING.")
                        }
                        Resource.Status.ERROR -> {
                            Log.d(TAG, "[LogInViewModel] Login response status is ERROR.")
                        }
                    }
                    Log.d(TAG, "[LogInViewModel] onChanged, success: ${loginResponse?.data?.success}, token: ${loginResponse?.data?.token}")
                    loginObservable.value = loginResponse
                }
            })
    }

    fun logIn(email: String, password: String) = repository.logIn(email, password)

    fun getLogInObservable(): LiveData<Resource<LoginResponse>> {
        return loginObservable
    }
}