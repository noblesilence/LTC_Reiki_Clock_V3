package com.learnteachcenter.ltcreikiclockv3.model.remote

/**
 * Created by 3piCerberus on 24/04/2018.
 */

/**
 * Status of a resource that is provided to the UI.
 *
 *
 * These are usually created by the Repository classes where they return
 * `LiveData<Resource<T>>` to pass back the latest data to the UI with its fetch status.
 */
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}