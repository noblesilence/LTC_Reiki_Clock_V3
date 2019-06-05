package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter

interface ReikiSession {

    var stateObservable: MutableLiveData<State>
    var currentIndexObservable: MutableLiveData<Int>
    var timeLeftObservable: MutableLiveData<String>

    fun start(index: Int)

    fun pause()

    fun resume()

    fun stop()

    // Session enum class

    enum class State {
        STOPPED, PAUSED, RUNNING
    }
}