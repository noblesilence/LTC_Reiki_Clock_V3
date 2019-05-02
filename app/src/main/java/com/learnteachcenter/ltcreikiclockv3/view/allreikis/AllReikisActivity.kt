package com.learnteachcenter.ltcreikiclockv3.view.allreikis

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.view.reiki.ReikiActivity
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllReikisViewModel
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.content_all_reikis.*

class AllReikisActivity : AppCompatActivity() {

    private lateinit var viewModel: AllReikisViewModel

    private val adapter = ReikiAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(AllReikisViewModel::class.java)

        reikisRecyclerView.layoutManager = LinearLayoutManager(this)
        reikisRecyclerView.adapter = adapter

        viewModel.getAllReikisLiveData().observe(this, Observer { reikis ->
            reikis?.let {
                adapter.updateReikis(reikis)
            }
        })

        fab.setOnClickListener {
            startActivity(Intent(this, ReikiActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                viewModel.clearAllReikis()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
