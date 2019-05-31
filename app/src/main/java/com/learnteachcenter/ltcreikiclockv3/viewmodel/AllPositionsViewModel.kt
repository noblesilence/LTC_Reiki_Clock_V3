package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.repository.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter
import kotlin.concurrent.timer

class AllPositionsViewModel (val reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var positions: LiveData<List<Position>> = repository.getPositions(reikiId)

    // Reiki Session
    lateinit var reikiSession: ReikiSession

    fun initSession(positions: List<Position>) {
        reikiSession = ReikiSession(positions)
    }

    fun startSession(positionIndex: Int) {
        reikiSession.start(positionIndex)
    }

    fun pauseSession() {
        Log.d("Reiki", "Pause Reiki session")

        reikiSession.pause()
    }

    fun resumeSession() {

        reikiSession.resume()
    }

    fun stopSession() {
        Log.d("Reiki", "Stop Reiki session")

        reikiSession.stop()
    }
}