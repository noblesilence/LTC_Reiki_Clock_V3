package com.learnteachcenter.ltcreikiclockv3.position.list

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.util.NotificationUtil
import com.learnteachcenter.ltcreikiclockv3.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // TODO: when timer finishes, it should go to the next one
        // how to connect this with ReikiSession?
        NotificationUtil.showTimerExpired(context)

        PrefUtil.setTimerState(ReikiSession.State.STOPPED, context)
        PrefUtil.setAlarmSetTime(0, context)
    }
}