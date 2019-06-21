package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.State
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.ReikiSessionEvent.NONE
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.ReikiSessionEvent.STATE_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.ReikiSessionEvent.INDEX_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSession.ReikiSessionEvent.TIME_LEFT_CHANGED
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.position.AddPositionActivity
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_all_positions.*
import kotlinx.android.synthetic.main.list_item_position.view.*

class ReikiSessionActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var reikiId: String
    private lateinit var reikiTitle: String
    private lateinit var viewModel: ReikiSessionViewModel
    private val adapter = PositionsAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.wtf(TAG, "\n[ReikiSessionActivity] onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)
        setSupportActionBar(toolbar)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {

            Log.wtf(TAG, "[ReikiSessionActivity] EXTRA REIKI ID")

            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            reikiTitle = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)

            supportActionBar?.title = reikiTitle

            viewModel = ViewModelProviders.of(this, ReikiSessionViewModelFactory(reikiId)).get(ReikiSessionViewModel::class.java)

            initRecyclerView()
            subscribeToReikiAndAllPositions()
        }
    }

    private fun initRecyclerView() {
        positionsRecyclerView.layoutManager = LinearLayoutManager(this)
        positionsRecyclerView.adapter = adapter

        Log.wtf(TAG, "initRecyclerView")
    }

    private fun subscribeToReikiAndAllPositions() {
        viewModel.reikiAndAllPositions.observe(this, Observer<ReikiAndAllPositions> {
            adapter.setPositions(it?.positions)
            viewModel.initSession(it!!)
            subscribeToReikiSession()
            setUpListeners()

            Log.wtf(TAG, "subscribeToReikiAndAllPositions")
        })
    }

    private fun subscribeToReikiSession() {

        Log.wtf(TAG, "[ReikiSessionActivity] subscribeToReikiSession()")

        val reikiSession: ReikiSession? = viewModel.getReikiSession()

        /**
         * Reiki Session state changed due to
         *  1. Play clicked
         *  2. Pause clicked
         *  3. Stop clicked
         *  4. Session ends
         *
         */

        reikiSession?.eventLiveData?.observe(this, Observer {
            when(it) {
                STATE_CHANGED -> {
                    when(reikiSession.getState()) {
                        State.STOPPED -> {
                            // Update Buttons
                            changeToPlayButton()
                            fab_add_position.alpha = 1F
                            fab_stop.alpha = 0F

                            val previousIndex = reikiSession.getPreviousIndex()
                            val previousDuration = reikiSession.getPreviousDuration()

                            unhighlightItem(previousIndex, previousDuration)
                        }
                        State.RUNNING -> {
                            // Update Buttons
                            changeToPauseButton()
                            fab_add_position.alpha = 0F
                            fab_stop.alpha = 1F

                            val currentIndex: Int = reikiSession.getCurrentIndex()
                            val currentDuration: String = reikiSession.getTimeLeft()

                            highlightItem(currentIndex, currentDuration)
                        }
                        State.PAUSED -> {
                            // Update buttons
                            changeToPlayButton()
                            fab_add_position.alpha = 0F
                            fab_stop.alpha = 1F

                            val currentIndex = reikiSession.getCurrentIndex()
                            val currentDuration = reikiSession.getTimeLeft()

                            highlightItem(currentIndex, currentDuration)
                        }
                    }
                }

                INDEX_CHANGED -> {
                    // Highlight next, unhighlight current
                    val previousIndex = reikiSession.getPreviousIndex()
                    val previousDuration = reikiSession.getPreviousDuration()
                    val currentIndex = reikiSession.getCurrentIndex()
                    val currentDuration = reikiSession.getTimeLeft()

                    unhighlightItem(previousIndex, previousDuration)
                    highlightItem(currentIndex, currentDuration)
                }

                TIME_LEFT_CHANGED -> {
                    val currentIndex = reikiSession.getCurrentIndex()
                    val currentDuration = reikiSession.getTimeLeft()

                    highlightItem(currentIndex, currentDuration)
                }

                NONE -> {
                    // Do nothing
                }
            }
        })
    }

    private fun setUpListeners() {

        fab_play_pause.setOnClickListener {

            when(viewModel.getReikiSession()?.getState()) {
                State.STOPPED -> {
                    viewModel.startSession(0)
                }
                State.RUNNING -> {
                    viewModel.pauseSession()
                }
                State.PAUSED -> {
                    viewModel.resumeSession()
                }
            }

            fab_add_position.alpha = 0F
        }

        fab_stop.setOnClickListener {
            viewModel.stopSession()
        }

        if(NetworkUtil.isConnected(this)) {
            fab_add_position.setOnClickListener {
                fab_add_position.alpha = 1F
                val intent = Intent(this, AddPositionActivity::class.java)
                intent.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reikiId)
                intent.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reikiTitle)
                startActivity(intent)
            }
        } else {
            fab_add_position.alpha = 0F
        }
    }

    private fun highlightItem(itemIndex: Int, itemDuration: String) {
        val color = ContextCompat.getColor(this, R.color.colorHighlight)
        val image = ContextCompat.getDrawable(this, R.drawable.ic_pause)!!

        try {
            val holder: PositionsAdapter.ViewHolder =
                positionsRecyclerView.findViewHolderForAdapterPosition(itemIndex)
                        as PositionsAdapter.ViewHolder

            holder.itemView.setBackgroundColor(color)
            holder.itemView.icon_play_pause.setImageDrawable(image)
            holder.itemView.duration.text = itemDuration
        } catch(exception: Exception) {
            Log.wtf("Reiki", "[ReikiSessionActivity] highlightItem -> Exception: $exception")
        }
    }

    private fun unhighlightItem(itemIndex: Int, itemDuration: String) {
        val color = 0
        val image = ContextCompat.getDrawable(this, R.drawable.ic_play_circle)!!

        try {
            val holder: PositionsAdapter.ViewHolder =
                positionsRecyclerView.findViewHolderForAdapterPosition(itemIndex)
                        as PositionsAdapter.ViewHolder

            holder.itemView.setBackgroundColor(color)
            holder.itemView.icon_play_pause.setImageDrawable(image)
            holder.itemView.duration.text = itemDuration
        } catch(exception: Exception) {
            Log.wtf("Reiki", "[ReikiSessionActivity] highlightItem -> Exception: $exception")
        }
    }

    private fun changeToPlayButton() {
        fab_play_pause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun changeToPauseButton() {
        fab_play_pause.setImageResource(R.drawable.ic_pause)
    }
}