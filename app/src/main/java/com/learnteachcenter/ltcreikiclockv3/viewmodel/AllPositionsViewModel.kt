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
import kotlin.concurrent.timer

class AllPositionsViewModel (val reikiId: String) : ViewModel() {

    // Positions

    private val repository: ReikiRepository = Injection.provideReikiRepository()
    var positions: LiveData<List<Position>> = repository.getPositions(reikiId)

    // Timer

    enum class TimerState  {
        Stopped, Paused, Running
    }

    // Timer variables
    private lateinit var countDownTimer: CountDownTimer
    private var currentPosition: Int = 0
    private var timerState = TimerState.Stopped
    private var secondsLeft = 0L

    // Observables
    var currentPositionObservable = MutableLiveData<Int>().apply { value = currentPosition }
    var timerStateObservable = MutableLiveData<TimerState>().apply { value = timerState}
    var timeLeftObservable = MutableLiveData<String>()

    // Timer methods

    fun initTimer(currentPosition: Int) {
        val countDownTime = positions.value?.get(currentPosition)?.duration
        secondsLeft = TimeFormatter.getSeconds(countDownTime!!)
        timeLeftObservable.value = TimeFormatter.getMinutesSeconds(secondsLeft)
    }

    fun startTimer() {
        startCountDown(secondsLeft)

        timerState = TimerState.Running
        timerStateObservable.value = timerState
    }

    fun startCountDown(timerDuration: Long) {
        countDownTimer = object: CountDownTimer(timerDuration * 1000,
            1000) {

            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = millisUntilFinished / 1000
                timeLeftObservable.value = TimeFormatter.getMinutesSeconds(secondsLeft)
            }

            override fun onFinish() {
                Log.d("Reiki", "Timer finished")

                onCountDownFinish()
            }
        }.start()
    }

    fun onCountDownFinish() {

        val lastIndex = positions.value?.size!! - 1

        if( currentPosition < lastIndex) {

            currentPosition++
            currentPositionObservable.value = currentPosition

            val countDownTime = positions.value?.get(currentPosition)?.duration

            secondsLeft = TimeFormatter.getSeconds(countDownTime!!)
            timeLeftObservable.value = TimeFormatter.getMinutesSeconds(secondsLeft)

            startCountDown(secondsLeft)
        }
        else {
            timerState = TimerState.Stopped
            timerStateObservable.value = timerState
        }
    }

    fun stopTimer() {
        countDownTimer.cancel()

        currentPosition = 0
        currentPositionObservable.value = currentPosition

        timerState = TimerState.Stopped
        timerStateObservable.value = timerState
    }

    fun pauseTimer() {
        countDownTimer.cancel()
        timerState = TimerState.Paused
        timerStateObservable.value = timerState
    }
}