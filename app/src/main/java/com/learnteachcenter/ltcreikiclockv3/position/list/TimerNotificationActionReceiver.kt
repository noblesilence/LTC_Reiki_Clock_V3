package com.learnteachcenter.ltcreikiclockv3.position.list

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.util.NotificationUtil
import com.learnteachcenter.ltcreikiclockv3.util.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action){
            AppConstants.ACTION_STOP -> {
                ReikiSession.removeAlarm(context)
                PrefUtil.setTimerState(ReikiSession.State.STOPPED, context)
                NotificationUtil.hideTimerNotification(context)
            }
            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = ReikiSession.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                ReikiSession.removeAlarm(context)
                PrefUtil.setTimerState(ReikiSession.State.PAUSED, context)
                NotificationUtil.showTimerPaused(context)
            }
            AppConstants.ACTION_RESUME -> {
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = ReikiSession.setAlarm(context, ReikiSession.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(ReikiSession.State.RUNNING, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
            AppConstants.ACTION_START -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = ReikiSession.setAlarm(context, ReikiSession.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(ReikiSession.State.RUNNING, context)
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
        }
    }
}