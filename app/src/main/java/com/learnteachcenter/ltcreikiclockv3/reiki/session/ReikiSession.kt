package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

interface ReikiSession {

    var eventLiveData: MutableLiveData<ReikiSessionEvent>

    fun getState(): State
    fun getCurrentIndex(): Int
    fun getPreviousIndex(): Int
    fun getTimeLeft(): String
    fun getPreviousDuration(): String

//    var stateObservable: MutableLiveData<State>
//    var currentIndexObservable: MutableLiveData<Int>
//    var timeLeftObservable: MutableLiveData<String>

//    fun getStateObservable(): LiveData<State>
//    fun getPreviousIndexObservable(): LiveData<Int>
//    fun getCurrentIndexObservable(): LiveData<Int>
//    fun getTimeLeftObservable(): LiveData<String>

    fun start(index: Int)

    fun pause()

    fun resume()

    fun stop()

    enum class State {
        STOPPED, PAUSED, RUNNING
    }

    enum class ReikiSessionEvent {
        NONE, STATE_CHANGED, INDEX_CHANGED, TIME_LEFT_CHANGED
    }
}