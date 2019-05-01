package com.learnteachcenter.ltcreikiclockv3.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.view.reiki.ReikiActivity
import kotlinx.android.synthetic.main.activity_all_reikis.*

class AllReikisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)

        fab.setOnClickListener {
            startActivity(Intent(this, ReikiActivity::class.java))
        }
    }
}
