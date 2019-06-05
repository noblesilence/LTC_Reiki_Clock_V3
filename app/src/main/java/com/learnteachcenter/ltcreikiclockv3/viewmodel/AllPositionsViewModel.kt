package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.model.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.repository.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter
import kotlin.concurrent.timer

class AllPositionsViewModel (private val reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var reikiAndAllPositions: LiveData<ReikiAndAllPositions> = repository.getReiki(reikiId)

    // Reiki Session
    lateinit var reikiSession: ReikiSession

    fun initSession(reiki: ReikiAndAllPositions) {
        reikiSession = ReikiSession(reiki)
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