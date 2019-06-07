package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.app.Injection

class ReikiSessionViewModel (private val reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var reikiAndAllPositions: LiveData<ReikiAndAllPositions> = repository.getReiki(reikiId)

    // Reiki Session
    lateinit var reikiSession: ReikiSession

    fun initSession(reiki: ReikiAndAllPositions) {
        reikiSession = ReikiSessionImpl(reiki)
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