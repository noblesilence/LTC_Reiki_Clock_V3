package com.learnteachcenter.ltcreikiclockv3.reikisession

import android.arch.lifecycle.MutableLiveData
import android.content.Context

interface ReikiSession {

    var eventLiveData: MutableLiveData<ReikiSessionEvent>

    fun getState(): State
    fun getCurrentIndex(): Int
    fun getPreviousIndex(): Int
    fun getTimeLeft(): String
    fun getPreviousDuration(): String

    fun start(index: Int)
    fun pause()
    fun runInBackground(context: Context)
    fun runInForeground(context: Context)
    fun resume()
    fun stop()

    enum class State { STOPPED, PAUSED, RUNNING }
    enum class ReikiSessionEvent { NONE, STATE_CHANGED, INDEX_CHANGED, TIME_LEFT_CHANGED }
}