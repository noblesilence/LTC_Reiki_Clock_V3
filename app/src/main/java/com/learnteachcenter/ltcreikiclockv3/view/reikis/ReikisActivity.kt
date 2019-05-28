package com.learnteachcenter.ltcreikiclockv3.view.reikis

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.model.authentication.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.model.reikis.Reiki
import com.learnteachcenter.ltcreikiclockv3.app.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.model.datasources.remote.ResourceObserver
import com.learnteachcenter.ltcreikiclockv3.view.allpositions.AllPositionsActivity
import com.learnteachcenter.ltcreikiclockv3.view.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.view.reiki.ReikiActivity
import com.learnteachcenter.ltcreikiclockv3.viewmodel.ReikisViewModel
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.content_all_reikis.*

class ReikisActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var viewModel: ReikisViewModel

    private val adapter = ReikiAdapter(mutableListOf()) {  reiki: Reiki ->
            reikiItemClicked(reiki)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(ReikisViewModel::class.java)

        initRecyclerView()
        subscribeObservers()

        // Retry button
        retryButton.setOnClickListener {
            Log.d(TAG, "Should retry API call")
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

    private fun initRecyclerView() {
        reikisRecyclerView.layoutManager = LinearLayoutManager(this)
        reikisRecyclerView.adapter = adapter
    }

    private fun subscribeObservers(){
        viewModel.reikis.observe(this, ResourceObserver(
            "ReikisActivity",
            hideLoading = ::hideLoading,
            showLoading = ::showLoading,
            onSuccess = ::showReikis,
            onError = ::showErrorMessage
        )
        )
    }

    private fun showReikis(reikis: List<Reiki>) {
        adapter.setReikis(reikis)
    }

    private fun showErrorMessage(error: String) {
        Log.d(TAG, "Error getting reikis: $error")
    }

    private fun showLoading() {
        progressBar.visibility = VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = GONE
    }

    fun reikiItemClicked(reiki: Reiki) {
        Toast.makeText(this, "Clicked: ${reiki.title}", Toast.LENGTH_LONG).show()

        val i = Intent(this, AllPositionsActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)

        startActivity(i)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
//                viewModel.deleteAllReikisInDB()
                true
            }
            R.id.action_logout -> {
                AuthenticationPrefs.clearAuthToken()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}