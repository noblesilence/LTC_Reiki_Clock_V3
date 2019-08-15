package com.learnteachcenter.ltcreikiclockv3.position.list

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSessionImpl

class PositionListViewModel (reikiId: String) : ViewModel() {
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
        if(::reikiSession.isInitialized) {
            reikiSession.start(positionIndex)
        }
    }

    fun pauseSession() {
        if(::reikiSession.isInitialized) {
            reikiSession.pause()
        }
    }

    fun resumeSession() {
        if(::reikiSession.isInitialized) {
            reikiSession.resume()
        }
    }

    fun stopSession() {
        if(::reikiSession.isInitialized) {
            reikiSession.stop()
        }
    }

    fun deletePositions(reikiId: String, vararg positions: Position) {
        repository.deletePositions(reikiId, *positions)
    }
}