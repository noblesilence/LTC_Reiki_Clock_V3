package com.learnteachcenter.ltcreikiclockv3.model.authentication

import android.preference.PreferenceManager
import com.learnteachcenter.ltcreikiclockv3.app.ReikiApplication

object AuthenticationPrefs {

    private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"

    private const val KEY_USERNAME = "KEY_USERNAME"

    private const val KEY_EMAIL = "KEY_EMAIL"

    private fun sharedPrefs() = PreferenceManager.getDefaultSharedPreferences(ReikiApplication.getAppContext())

    fun saveAuthToken(token: String) {
        val editor = sharedPrefs().edit()
        editor.putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String = sharedPrefs().getString(
        KEY_AUTH_TOKEN, "")

    fun clearAuthToken() = sharedPrefs().edit().remove(
        KEY_AUTH_TOKEN
    ).apply()

    fun isAuthenticated() = !getAuthToken().isBlank()

    fun saveUsername(username: String) {
        val editor = sharedPrefs().edit()
        editor.putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String = sharedPrefs().getString(
        KEY_USERNAME, "Reiki User")

    fun clearUsername() = sharedPrefs().edit().remove(
        KEY_USERNAME
    ).apply()

    fun saveEmail(email: String) {
        val editor = sharedPrefs().edit()
        editor.putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String = sharedPrefs().getString(
        KEY_EMAIL, "")

    fun clearEmail() = sharedPrefs().edit().remove(
        KEY_EMAIL
    ).apply()

}