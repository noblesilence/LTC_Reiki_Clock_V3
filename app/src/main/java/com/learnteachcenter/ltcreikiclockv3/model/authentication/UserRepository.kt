package com.learnteachcenter.ltcreikiclockv3.model.authentication

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.Resource
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.Resource.Status.SUCCESS
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.Resource.Status.LOADING
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.Resource.Status.ERROR
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: How can I test only signup/login feature?

object UserRepository {

    private val TAG = "Reiki"

    val signUpObservable = MutableLiveData<Resource<User>>()
    val logInObservable = MutableLiveData<Resource<LoginResponse>>()

    private val reikiApi: ReikiApi = Injection.provideReikiApi()

    fun signUp(user: User) {
        reikiApi.signUp(
            user.name,
            user.email,
            user.password,
            user.password2).enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    setSignUpObservableData(response.body()!!,"")
                    setSignUpObservableStatus(SUCCESS,"")

                    Log.d(TAG, "[ReikiRepository] Success registration")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setSignUpObservableStatus(ERROR,response.code().toString() + " " + jObjError)

                    Log.d(TAG, "[ReikiRepository] Error message from the API: " + response.code().toString() + " " + jObjError)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, t.message)
                setSignUpObservableStatus(ERROR, t.message!!)
            }
        })
    }

    fun setSignUpObservableData(user: User, message: String) {
        var loadingStatus = LOADING
        if (signUpObservable.value != null) {
            loadingStatus = signUpObservable.value!!.status
        }
        when (loadingStatus) {
            LOADING -> signUpObservable.setValue(Resource.loading(user))
            SUCCESS -> signUpObservable.setValue(Resource.success(user))
            ERROR -> signUpObservable.setValue(Resource.error(message, user))
        }
    }

    private fun setSignUpObservableStatus(status: Resource.Status, message: String) {

        var loadingUser: User? = null
        if(signUpObservable.value != null) {
            loadingUser = signUpObservable.value?.data
        }
        when(status) {
            LOADING -> signUpObservable.value = Resource.loading(loadingUser)
            SUCCESS -> {
                if(loadingUser != null) {
                    signUpObservable.value = Resource.success(loadingUser)
                }
            }
            ERROR -> signUpObservable.value = Resource.error(message, loadingUser)
        }
    }

    /**
     * LOGIN
     */
    fun logIn(email: String, password: String) {
        reikiApi.logIn(email, password).enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful) {
                    setLogInObservableData(response.body()!!,"")
                    setLogInObservableStatus(SUCCESS,"")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setLogInObservableStatus(ERROR, jObjError.toString())

                    Log.d(TAG,"[ReikiRepository] Error logging in: Status $response.code().toString() $jObjError")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d(TAG, t.message)
                setLogInObservableStatus(ERROR, t.message!!)
            }
        })
    }

    fun setLogInObservableData(loginResponse: LoginResponse, message: String) {
        var loadingStatus = LOADING
        if(logInObservable.value != null) {
            loadingStatus = logInObservable.value!!.status
        }
        when(loadingStatus) {
            LOADING -> logInObservable.value = Resource.loading(loginResponse)
            SUCCESS -> logInObservable.value = Resource.success(loginResponse)
            ERROR -> logInObservable.value = Resource.error(message, loginResponse)
        }
    }

    fun setLogInObservableStatus(status: Resource.Status, message: String) {
        var loadingLoginResponse: LoginResponse? = null
        if(logInObservable.value != null) {
            loadingLoginResponse = logInObservable.value?.data
        }
        when(status) {
            LOADING -> logInObservable.value = Resource.loading(loadingLoginResponse)
            SUCCESS -> {
                if(loadingLoginResponse != null) {
                    logInObservable.value = Resource.success(loadingLoginResponse)
                }
            }
            ERROR -> logInObservable.value = Resource.error(message, loadingLoginResponse)
        }
    }
}