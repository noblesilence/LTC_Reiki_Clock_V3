package com.learnteachcenter.ltcreikiclockv3.util

import android.arch.lifecycle.Observer
import android.util.Log

class ResourceObserver<T>(private val tag: String,
                          private val hideLoading: () -> Unit,
                          private val showLoading: () -> Unit,
                          private val onSuccess: (data: T) -> Unit,
                          private val onError: (message: String) -> Unit) : Observer<Resource<T>> {

    override fun onChanged(resource: Resource<T>?) {
        when(resource?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if(resource.data != null) {
                    Log.d(tag, "observer -> SUCCESS, ${resource.data} items")
                    onSuccess(resource.data)
                }
            }
            Resource.Status.ERROR -> {
                hideLoading()
                if(resource.message != null) {
                    Log.d(tag, "observer -> ERROR, ${resource.message}")
                    onError(resource.message)
                }
            }
            Resource.Status.LOADING -> {
                showLoading()
                Log.d(tag, "observer -> LOADING")
            }
        }
    }
}