package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.MutableLiveData
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter

// TODO: add sound

class ReikiSession (private val positions: List<Position>) {

    // Private Variables
    private var currentIndex = -1
    private lateinit var countDownTimer: CountDownTimer
    private var timerState = TimerState.Stopped
    private var secondsLeft = 0L

    // Public Observables
    var timerStateObservable = MutableLiveData<TimerState>().apply() {
        value = timerState
    }
    var currentIndexObservable = MutableLiveData<Int>().apply() {
        value = currentIndex
    }
    var timeLeftObservable = MutableLiveData<String>().apply() {
        value = "00:00"
    }

    // Public methods

    fun start(index: Int) {
        currentIndex = index
        currentIndexObservable.value = currentIndex

        val countDownTime = positions.get(currentIndex).duration
        secondsLeft = TimeFormatter.getSeconds(countDownTime)

        startCountDown(secondsLeft)

        timerState = TimerState.Running
        timerStateObservable.value = timerState
    }

    fun resume() {
        startCountDown(secondsLeft)

        timerState = TimerState.Running
        timerStateObservable.value = timerState
    }

    fun stop() {
        countDownTimer.cancel()

        currentIndex = -1
        currentIndexObservable.value = currentIndex

        timerState = TimerState.Stopped
        timerStateObservable.value = timerState
    }

    fun pause() {
        countDownTimer.cancel()
        timerState = TimerState.Paused
        timerStateObservable.value = timerState
    }

    private fun startCountDown(timerDuration: Long) {
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

    private fun onCountDownFinish() {

        val lastIndex = positions.size - 1

        if(currentIndex < lastIndex) {

            currentIndex++
            currentIndexObservable.value = currentIndex

            val countDownTime = positions.get(currentIndex).duration

            secondsLeft = TimeFormatter.getSeconds(countDownTime)

            startCountDown(secondsLeft)
        }
        else {
            timerState = TimerState.Stopped
            timerStateObservable.value = timerState
        }
    }

    // TimerState enum class

    enum class TimerState {
        Stopped, Paused, Running
    }
}