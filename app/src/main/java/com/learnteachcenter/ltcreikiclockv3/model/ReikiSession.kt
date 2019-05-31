package com.learnteachcenter.ltcreikiclockv3.model

import android.os.CountDownTimer

class ReikiSession {

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var positions: List<Position>
    private lateinit var timerState: TimerState
    private var currentPositionIndex: Int = 0
    private var secondsLeft = 0L

    enum class TimerState {
        Stopped, Paused, Running
    }
}