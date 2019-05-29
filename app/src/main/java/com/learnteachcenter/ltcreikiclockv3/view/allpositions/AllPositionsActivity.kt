package com.learnteachcenter.ltcreikiclockv3.view.allpositions

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.util.IntentExtraNames

class AllPositionsActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var reikiId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            Log.d(TAG, "reikiId is ${reikiId}")
        }
    }
}
