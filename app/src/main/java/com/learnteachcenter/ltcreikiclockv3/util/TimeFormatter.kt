package com.learnteachcenter.ltcreikiclockv3.util

import android.util.Log
import java.lang.Exception

object TimeFormatter {

    // Convert from Seconds to Minutes:Seconds
    fun getMinutesSeconds(seconds: Long): String {
        val minutes = seconds / 60
        val seconds = seconds % 60
        var minutesStr = minutes.toString()
        var secondsStr = seconds.toString()

        if(minutes < 10) {
            minutesStr = "0" + minutesStr
        }

        if(seconds < 10) {
            secondsStr = "0" + secondsStr
        }

        return "$minutesStr:$secondsStr"
    }

    // Convert from Minutes:Seconds to Seconds
    fun getSeconds(minutesSeconds: String): Long {
        val minutesSecondsArray = minutesSeconds.split(":")
        var seconds: Long = 0

        try {
            if(minutesSecondsArray.size == 2) {
                seconds = (minutesSecondsArray[0].toLong() * 60) + minutesSecondsArray[1].toLong()
            } else if(minutesSecondsArray.size == 1) {
                seconds = minutesSecondsArray[0].toLong()
            }
        } catch(exception: Exception) {
            Log.d("Reiki", "Not a time string")
        }

        return seconds
    }
}