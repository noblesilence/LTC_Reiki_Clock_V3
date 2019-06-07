package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.MutableLiveData

interface ReikiSession {

    var stateObservable: MutableLiveData<State>
    var currentIndexObservable: MutableLiveData<Int>
    var timeLeftObservable: MutableLiveData<String>

    fun start(index: Int)

    fun pause()

    fun resume()

    fun stop()

    enum class State {
        STOPPED, PAUSED, RUNNING
    }
}