package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.repository.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter

class AllPositionsViewModel (reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var positions: LiveData<List<Position>> = repository.getPositions(reikiId)

    // Timer

    enum class TimerState  {
        Stopped, Paused, Running
    }

    // Observables
    var currentPosition = MutableLiveData<Int>().apply { value = 0 }
    var timerState = MutableLiveData<TimerState>().apply { value = TimerState.Stopped }
    var secondsRemaining = MutableLiveData<Long>().apply { value = 0 }

    // Timer internals
    private lateinit var countDownTimer: CountDownTimer

    fun initTimer(currentPosition: Int) {
        val countDownTime = positions.value?.get(currentPosition)?.duration
        secondsRemaining.value = TimeFormatter.getSeconds(countDownTime!!)
    }

    fun startTimer() {
        countDownTimer = object: CountDownTimer(secondsRemaining.value!! * 1000,
            1000) {

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                Log.d("Reiki", "Timer finished")
            }
        }.start()

        timerState.value = TimerState.Running
    }

    fun stopTimer() {
        countDownTimer.cancel()
        timerState.value = TimerState.Stopped
    }

    fun pauseTimer() {
        countDownTimer.cancel()
        timerState.value = TimerState.Paused
    }
}