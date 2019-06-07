package com.learnteachcenter.ltcreikiclockv3.util

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected
        return isConnected
    }
}