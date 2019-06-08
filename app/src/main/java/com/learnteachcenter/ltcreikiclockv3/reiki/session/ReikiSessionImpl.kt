package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.util.TimeFormatter
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.ReikiSessionEvent
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.RUNNING
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.PAUSED
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State.STOPPED

class ReikiSessionImpl (private val reikiAndAllPositions: ReikiAndAllPositions,
                    private val context: Context = Injection.provideContext()
): ReikiSession {

    // Private Variables
    private var state = STOPPED
    private var currentIndex = -1
    private var previousIndex = -1
    private var previousDuration: String = "00:00"
    private var countDownTimer: CountDownTimer? = null
    private var secondsLeft = 0L
    private var secondsFinishing = 3

    override fun getState() = state
    override fun getCurrentIndex(): Int = currentIndex
    override fun getPreviousIndex(): Int = previousIndex
    override fun getTimeLeft(): String = TimeFormatter.getMinutesSeconds(secondsLeft)
    override fun getPreviousDuration(): String = previousDuration

    override var eventLiveData = MutableLiveData<ReikiSessionEvent>().apply {
        value = ReikiSessionEvent.NONE
    }

//    override var stateObservable = MutableLiveData<State>().apply { value = state }
//    override var currentIndexObservable = MutableLiveData<Int>().apply { value = currentIndex }
//    override var timeLeftObservable = MutableLiveData<String>().apply { value = "00:00" }

//    override fun getStateObservable(): LiveData<State> = stateObservable
//    override fun getCurrentIndexObservable(): LiveData<Int> = currentIndexObservable
//    override fun getPreviousIndexObservable(): LiveData<Int> = previousIndexObservable
//    override fun getTimeLeftObservable(): LiveData<String>  = timeLeftObservable

    private var bgMusicPlayer: MediaPlayer? = null

    // Override methods
    override fun start(index: Int) {

        state = RUNNING
        previousIndex = currentIndex
        currentIndex = index

        val countDownTime = reikiAndAllPositions.positions.get(currentIndex).duration
        secondsLeft = TimeFormatter.getSeconds(countDownTime)
        previousDuration = countDownTime

        startCountDown(secondsLeft)
        playBackgroundSound()

        eventLiveData.value = ReikiSessionEvent.STATE_CHANGED
    }

    override fun pause() {
        countDownTimer!!.cancel()
        pauseBackgroundSound()

        state = PAUSED

        eventLiveData.value = ReikiSessionEvent.STATE_CHANGED
    }

    override fun resume() {
        startCountDown(secondsLeft)
        playBackgroundSound()

        state = RUNNING

        eventLiveData.value = ReikiSessionEvent.STATE_CHANGED
    }

    override fun stop() {

        if(countDownTimer != null) {
            countDownTimer!!.cancel()
            stopBackgroundSound()

            state = STOPPED
            previousIndex = currentIndex
            currentIndex = -1
            previousDuration =  reikiAndAllPositions.positions.get(previousIndex).duration

            eventLiveData.value = ReikiSessionEvent.STATE_CHANGED
        }
    }

    // Private methods

    private fun startCountDown(timerDuration: Long) {
        countDownTimer = object: CountDownTimer(timerDuration * 1000,
            1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = millisUntilFinished / 1000

                eventLiveData.value = ReikiSessionEvent.TIME_LEFT_CHANGED
            }

            override fun onFinish() {
                onCountDownFinish()
            }
        }.start()
    }

    private fun onCountDownFinish() {

        playReminderSound()

        val lastIndex = reikiAndAllPositions.positions.size - 1

        if(currentIndex < lastIndex) {

            previousIndex = currentIndex
            currentIndex++

            val countDownTime = reikiAndAllPositions.positions.get(currentIndex).duration
            secondsLeft = TimeFormatter.getSeconds(countDownTime)
            previousDuration = countDownTime

            startCountDown(secondsLeft)

            eventLiveData.value = ReikiSessionEvent.INDEX_CHANGED
        }
        else {
            state = STOPPED
            previousIndex = currentIndex
            currentIndex = -1

            fadeAndStopBackgroundSound()

            eventLiveData.value = ReikiSessionEvent.STATE_CHANGED
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
                    secondsFinishing = 3
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