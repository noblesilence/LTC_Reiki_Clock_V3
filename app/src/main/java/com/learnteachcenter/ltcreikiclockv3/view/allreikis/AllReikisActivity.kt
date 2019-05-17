package com.learnteachcenter.ltcreikiclockv3.view.allreikis

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.se.omapi.Session
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.basic.Status
import com.learnteachcenter.ltcreikiclockv3.model.basic.Resource
import com.learnteachcenter.ltcreikiclockv3.model.session.SessionManager
import com.learnteachcenter.ltcreikiclockv3.network.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.view.reiki.ReikiActivity
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllReikisViewModel
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.content_all_reikis.*

class AllReikisActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var viewModel: AllReikisViewModel

    private val adapter = ReikiAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)

        setSupportActionBar(toolbar)

        reikisRecyclerView.layoutManager = LinearLayoutManager(this)
        reikisRecyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(AllReikisViewModel::class.java)
        viewModel.getReikisObservable().observe(this, Observer<Resource<List<Reiki>>> { resource ->
            if(resource?.data != null) {
                adapter.updateReikis(resource.data)
                progressBar.visibility = View.GONE
                errorTextView.visibility = View.GONE
                retryButton.visibility = View.GONE
            }
            else {
                println("Error getting reikis")
                if(resource?.status == Status.ERROR) {
                    Toast.makeText(this, getString(R.string.error_retrieving_reikis), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        })
        viewModel.getReikis()

        retryButton.setOnClickListener {
            Log.d(TAG, "Should retry API call")
            viewModel.getReikis()
        }

        // Show/hide Add button based on internet connectivity
        if(NetworkUtil.isConnected(this)) {
            fab.setOnClickListener {
                startActivity(Intent(this, ReikiActivity::class.java))
            }
        } else {
            fab.hide()
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
                viewModel.deleteAllReikisInDB()
                true
            }
            R.id.action_logout -> {
                val session = SessionManager(this)
                session.logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}