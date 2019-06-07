package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.util.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.RUNNING
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.PAUSED
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.STOPPED

class ReikiSessionImpl (private val reikiAndAllPositions: ReikiAndAllPositions,
                    private val context: Context = Injection.provideContext()
): ReikiSession {

    // Private Variables
    private var currentIndex = -1
    private lateinit var countDownTimer: CountDownTimer
    private var state = STOPPED
    private var secondsLeft = 0L
    private var secondsFinishing = 3

    override var stateObservable = MutableLiveData<ReikiSession.State>().apply() { value = state }
    override var currentIndexObservable = MutableLiveData<Int>().apply() { value = currentIndex }
    override var timeLeftObservable = MutableLiveData<String>().apply() { value = "00:00" }

    private var bgMusicPlayer: MediaPlayer? = null

    // Override methods
    override fun start(index: Int) {
        currentIndex = index
        currentIndexObservable.value = currentIndex

        val countDownTime = reikiAndAllPositions.positions.get(currentIndex).duration
        secondsLeft = TimeFormatter.getSeconds(countDownTime)

        startCountDown(secondsLeft)
        playBackgroundSound()

        state = RUNNING
        stateObservable.value = state
    }

    override fun pause() {
        countDownTimer.cancel()
        pauseBackgroundSound()
        state = PAUSED
        stateObservable.value = state
    }

    override fun resume() {
        startCountDown(secondsLeft)
        playBackgroundSound()

        state = RUNNING
        stateObservable.value = state
    }

    override fun stop() {

        countDownTimer.cancel()
        stopBackgroundSound()

        timeLeftObservable.value = reikiAndAllPositions.positions.get(currentIndex).duration

        currentIndex = -1
        currentIndexObservable.value = currentIndex

        state = STOPPED
        stateObservable.value = state
    }

    // Private methods

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

        playReminderSound()

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
            state = STOPPED
            stateObservable.value = state

            timeLeftObservable.value = reikiAndAllPositions.positions.get(currentIndex).duration

            currentIndex = -1
            currentIndexObservable.value = currentIndex

            fadeAndStopBackgroundSound()
        }
    }

    private fun fadeAndStopBackgroundSound() {
        if(reikiAndAllPositions.reiki!!.playMusic) {

            object: CountDownTimer(4000, 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    if(bgMusicPlayer != null) {
                        val v = secondsFinishing / 4F
                        bgMusicPlayer?.setVolume(v, v)
                        secondsFinishing--
                    }
                }

                override fun onFinish() {
                    this.cancel()
                    stopBackgroundSound()
                }
            }.start()
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

    /**
     * Method to play Reminder Sound
     */
    private fun playReminderSound() {
        val mpReminderSound = MediaPlayer.create(context, R.raw.reminder_sound)
        mpReminderSound.start()
    }
}