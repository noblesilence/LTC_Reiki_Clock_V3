package com.learnteachcenter.ltcreikiclockv3.view.allpositions

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.util.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModel
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModelFactory
import kotlinx.android.synthetic.main.activity_all_positions.*

class AllPositionsActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var reikiId: String

    private lateinit var viewModel: AllPositionsViewModel

    private val adapter = PositionsAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)
        setSupportActionBar(toolbar)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            Log.d(TAG, "reikiId is ${reikiId}")

            viewModel = ViewModelProviders.of(this, AllPositionsViewModelFactory(reikiId))
                .get(AllPositionsViewModel::class.java)

            initRecyclerView()
            subscribeObservers()

            // Show/hide Add button based on internet connectivity
            if(NetworkUtil.isConnected(this)) {
                fab_add.setOnClickListener {
                }
            } else {
                fab_add.hide()
            }
        }
    }

    private fun initRecyclerView() {
        positionsRecyclerView.layoutManager = LinearLayoutManager(this)
        positionsRecyclerView.adapter = adapter
    }

    private fun subscribeObservers() {
        viewModel.positions.observe(this, Observer<List<Position>> {positions ->
            adapter.setPositions(positions)
        })
    }

}
