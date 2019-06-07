package com.learnteachcenter.ltcreikiclockv3.splash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.database.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.reiki.all.AllReikisActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val i:Intent

        if(AuthenticationPrefs.isAuthenticated()) {
            i = Intent(this, AllReikisActivity::class.java)
        } else {
            i = Intent(this, LoginActivity::class.java)
        }

        startActivity(i)
        finish()
    }
}
