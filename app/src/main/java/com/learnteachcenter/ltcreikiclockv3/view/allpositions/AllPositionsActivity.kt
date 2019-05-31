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
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModel.TimerState
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModelFactory
import kotlinx.android.synthetic.main.activity_all_positions.*

class AllPositionsActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var reikiId: String
    private lateinit var reikiTitle: String

    private lateinit var timerState: TimerState
    private var currentPosition: Int = 0

    private lateinit var viewModel: AllPositionsViewModel

    private val adapter = PositionsAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)
        setSupportActionBar(toolbar)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            reikiTitle = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)

            supportActionBar?.title = reikiTitle

            viewModel = ViewModelProviders.of(this, AllPositionsViewModelFactory(reikiId))
                .get(AllPositionsViewModel::class.java)

            initRecyclerView()
            subscribeObservers()
            setUpListeners()

            // Show/hide Add button based on internet connectivity
            if(NetworkUtil.isConnected(this)) {
                fab_add.alpha = 1F
                fab_add.setOnClickListener {
                }
            } else {
                fab_add.alpha = 0F
            }
        }
    }

    private fun initRecyclerView() {
        positionsRecyclerView.layoutManager = LinearLayoutManager(this)
        positionsRecyclerView.adapter = adapter
    }

    private fun subscribeObservers() {
        viewModel.positions.observe(this, Observer<List<Position>> {
            adapter.setPositions(it)
        })
        viewModel.timeLeftObservable.observe(this, Observer<String> {
            Log.d("Reiki", "Time remaining: $it")
        })
        viewModel.timerStateObservable.observe(this, Observer {
            timerState = it!!
            Log.d("Reiki", "Timer state is $timerState")
        })
        viewModel.currentPositionObservable.observe(this, Observer {
            currentPosition = it!!
            // TODO: use this current position value to highlight the current Position
        })
    }

    private fun setUpListeners() {
        fab_play_pause.setOnClickListener {

            Log.d("Reiki", "Play/pause clicked")

            when(timerState) {
                TimerState.Stopped -> {
                    viewModel.initTimer(currentPosition)
                    viewModel.startTimer()

                    changeToPauseButton()

                    Log.d("Reiki", "Timer started")
                }
                TimerState.Running -> {
                    viewModel.pauseTimer()

                    changeToPlayButton()

                    Log.d("Reiki", "Timer paused")
                }
                TimerState.Paused -> {
                    viewModel.startTimer()

                    changeToPauseButton()

                    Log.d("Reiki", "Timer started")
                }
            }

            hideAddButton()
        }

        fab_stop.setOnClickListener {
            viewModel.stopTimer()

            changeToPlayButton()
            showAddButton()

            Log.d("Reiki", "Timer stopped")
        }

        fab_add.setOnClickListener {
            onAddClick()
            Log.d("Reiki", "Add Position clicked")
        }
    }

    private fun onAddClick() {

    }

    private fun changeToPlayButton() {
        fab_play_pause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun changeToPauseButton() {
        fab_play_pause.setImageResource(R.drawable.ic_pause)
    }

    private fun showAddButton() {
        fab_add.alpha = 1F
    }

    private fun hideAddButton() {
        fab_add.alpha = 0F
    }
}