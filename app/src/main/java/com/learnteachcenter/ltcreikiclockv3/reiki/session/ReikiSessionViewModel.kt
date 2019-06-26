package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.app.Injection

class ReikiSessionViewModel (private val reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var reikiAndAllPositions: LiveData<ReikiAndAllPositions> = repository.getReiki(reikiId)

    // Reiki Session
    private lateinit var reikiSession: ReikiSession

    fun getReikiSession(): ReikiSession? {
      if(::reikiSession.isInitialized) {
          return reikiSession
      } else {
          return null
      }
    }

    fun initSession(reiki: ReikiAndAllPositions) {
        if(!::reikiSession.isInitialized) {
            reikiSession = ReikiSessionImpl(reiki)
        }
    }

    fun startSession(positionIndex: Int) {
        reikiSession.start(positionIndex)
    }

    fun pauseSession() {
        reikiSession.pause()
    }

    fun resumeSession() {
        reikiSession.resume()
    }

    fun stopSession() {
        reikiSession.stop()
    }

    fun deletePosition(reikiId: String, positionId: String) {
        repository.deletePosition(reikiId, positionId)
    }
}