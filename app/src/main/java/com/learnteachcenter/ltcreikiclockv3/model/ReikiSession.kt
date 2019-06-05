package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter

// TODO: add sound

class ReikiSession (private val reikiAndAllPositions: ReikiAndAllPositions,
                    private val context: Context = Injection.provideContext()
) {

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

    private var bgMusicPlayer: MediaPlayer? = null

    // Public methods

    fun start(index: Int) {
        currentIndex = index
        currentIndexObservable.value = currentIndex

        val countDownTime = reikiAndAllPositions.positions.get(currentIndex).duration
        secondsLeft = TimeFormatter.getSeconds(countDownTime)

        startCountDown(secondsLeft)
        playBackgroundSound()

        timerState = TimerState.Running
        timerStateObservable.value = timerState
    }

    fun resume() {
        startCountDown(secondsLeft)
        playBackgroundSound()

        timerState = TimerState.Running
        timerStateObservable.value = timerState
    }

    fun stop() {

        countDownTimer.cancel()
        stopBackgroundSound()

        timeLeftObservable.value = reikiAndAllPositions.positions.get(currentIndex).duration

        currentIndex = -1
        currentIndexObservable.value = currentIndex

        timerState = TimerState.Stopped
        timerStateObservable.value = timerState
    }

    fun pause() {
        countDownTimer.cancel()
        pauseBackgroundSound()
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

        val lastIndex = reikiAndAllPositions.positions.size - 1

        if(currentIndex < lastIndex) {

            timeLeftObservable.value = reikiAndAllPositions.positions.get(currentIndex).duration

            currentIndex++
            currentIndexObservable.value = currentIndex

            val countDownTime = reikiAndAllPositions.positions.get(currentIndex).duration

            secondsLeft = TimeFormatter.getSeconds(countDownTime)

            startCountDown(secondsLeft)
        }
        else {
            timerState = TimerState.Stopped
            timerStateObservable.value = timerState
        }
    }

    /**
     * Method to play Background Sound
     */
    private fun playBackgroundSound() {
        if (reikiAndAllPositions.reiki!!.playMusic) {
            try {
                if (bgMusicPlayer == null) {
                    bgMusicPlayer = MediaPlayer.create(context, R.raw.background_sound)
                    bgMusicPlayer?.setLooping(true)
                }

                bgMusicPlayer?.start()

                Log.d("Reiki", "[ReikiSession] playBackgroundSound")
            } catch (ex: Exception) {
                Log.wtf("DEBUG", "Exception in playBackgroundSound: $ex")
            }

        }
    }

    /**
     * Method to pause Background Sound
     */
    private fun pauseBackgroundSound() {
        if (bgMusicPlayer != null) {
            bgMusicPlayer?.pause()

            Log.d("Reiki", "[ReikiSession] pauseBackgroundSound")
        }
    }

    /**
     * Method to stop Background Sound
     */
    private fun stopBackgroundSound() {
        if (bgMusicPlayer != null) {

            if (bgMusicPlayer!!.isPlaying()) {
                bgMusicPlayer?.stop()
            }

            bgMusicPlayer?.release()
            bgMusicPlayer = null

            Log.d("Reiki", "[ReikiSession] stopBackgroundSound")
        }
    }

    // TimerState enum class

    enum class TimerState {
        Stopped, Paused, Running
    }
}