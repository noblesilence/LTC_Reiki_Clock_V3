package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.model.authentication.LoginResponse
import com.learnteachcenter.ltcreikiclockv3.model.authentication.User
import com.learnteachcenter.ltcreikiclockv3.model.remote.Resource
import com.learnteachcenter.ltcreikiclockv3.model.remote.Status
import com.learnteachcenter.ltcreikiclockv3.model.remote.Status.*
import com.learnteachcenter.ltcreikiclockv3.model.remote.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.utils.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.model.remote.Error
import com.learnteachcenter.ltcreikiclockv3.utils.NetworkUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject



// https://medium.com/@thanasakis/restful-api-consuming-on-android-using-retrofit-and-architecture-components-livedata-room-and-59e3b064f94
// https://stackoverflow.com/questions/32519618/retrofit-2-0-how-to-get-deserialised-error-response-body

class ReikiRepository constructor(
    private val reikiDao: ReikiDao = Injection.provideReikiDao(),
    private val reikiApi: ReikiApi = Injection.provideReikiApi(),
    private val appExecutors: AppExecutors = Injection.provideAppExecutors()
) {

    private val TAG = "Reiki"

    fun getReikis(): LiveData<Resource<List<Reiki>>> {
        return object : NetworkBoundResource<List<Reiki>>(appExecutors) {
            override fun saveNetworkCallResult(data: List<Reiki>?) {
                Log.d(TAG, "saveNetworkCallResult")
                data?.forEach {
                    reikiDao.insertReiki(it)
                    it.positions.forEach {
                        reikiDao.insertPosition(it)
                    }
                }
            }

            override fun shouldLoadFromNetwork(data: List<Reiki>?): Boolean {
                val shouldLoadFromNetwork = NetworkUtil.isConnected(Injection.provideContext()) && (data == null || data.isEmpty())
                Log.d(TAG, "shouldLoadFromNetwork: $shouldLoadFromNetwork")
                return shouldLoadFromNetwork
            }

            override fun loadFromDatabase(): LiveData<List<Reiki>> {
                return reikiDao.getReikis()
            }

            override fun createNetworkCall(): Call<List<Reiki>> {
                Log.d(TAG, "createNetworkCall")
                return reikiApi.getReikis()
            }
        }.asLiveData()
    }

    val signUpObservable = MutableLiveData<Resource<User>>()
    val logInObservable = MutableLiveData<Resource<LoginResponse>>()

    // TODO: maybe move this signup/login to a separate class UserRepository

    /**
     * SIGN UP
     */
    fun signUp(user: User) {
        reikiApi.signUp(
            user.name,
            user.email,
            user.password,
            user.password2).enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    setSignUpObservableData(response.body()!!, "")
                    setSignUpObservableStatus(SUCCESS, "")
                    Log.d(TAG, "[ReikiRepository] Success registration")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setSignUpObservableStatus(ERROR,
                        response.code().toString() + " " + jObjError)

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
            ERROR -> signUpObservable.setValue(Resource.error(user, Error(0,message)))
        }
    }

    private fun setSignUpObservableStatus(status: Status, message: String) {

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
            ERROR -> signUpObservable.value = Resource.error(loadingUser, Error(0, message))
        }
    }

    /**
     * LOGIN
     */
    fun logIn(email: String, password: String) {
        reikiApi.logIn(email, password).enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful) {
                    setLogInObservableData(response.body()!!, "")
                    setLogInObservableStatus(SUCCESS, "")
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    setLogInObservableStatus(ERROR, jObjError.toString())

                    Log.d(TAG,
                        "[ReikiRepository] Error logging in: Status $response.code().toString() $jObjError")
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
            ERROR -> logInObservable.value = Resource.error(loginResponse, Error(0, message))
        }
    }

    fun setLogInObservableStatus(status: Status, message: String) {
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
            ERROR -> logInObservable.value = Resource.error(loadingLoginResponse, Error(0, message))
        }
    }

    // Create
    fun addReiki(reiki: Reiki) {
        reikiDao.insertReiki(reiki)
    }

}