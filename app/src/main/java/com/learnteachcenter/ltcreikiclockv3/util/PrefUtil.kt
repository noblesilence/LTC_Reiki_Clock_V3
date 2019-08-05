package com.learnteachcenter.ltcreikiclockv3.util

import android.content.Context
import android.preference.PreferenceManager
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession

class PrefUtil {
    companion object {

        private const val TIMER_LENGTH_ID = "com.reikitimer.timer_length"
        private const val TIMER_STATE_ID = "com.reikitimer.timer_state"
        private const val SECONDS_REMAINING_ID = "com.resocoder.timer.seconds_remaining"
        private const val ALARM_SET_TIME_ID = "com.reikitimer.backgrounded_time"
        private const val CURRENT_POSITION_INDEX = "com.reikitimer.current_position_index"

        fun getTimerLength(context: Context): Int{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(TIMER_LENGTH_ID, 10)
        }

        fun getTimerState(context: Context): ReikiSession.State {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return ReikiSession.State.values()[ordinal]
        }

        fun setTimerState(state: ReikiSession.State, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        fun getSecondsRemaining(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        fun getAlarmSetTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmSetTime(time: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }

        fun getCurrentPositionIndex(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(CURRENT_POSITION_INDEX, 0)
        }

        fun setCurrentPositionIndex(index: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(CURRENT_POSITION_INDEX, index)
            editor.apply()
        }
    }
}