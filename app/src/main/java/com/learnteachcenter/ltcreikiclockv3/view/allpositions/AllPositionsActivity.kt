package com.learnteachcenter.ltcreikiclockv3.view.allpositions

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.model.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.model.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.model.ReikiSession.TimerState
import com.learnteachcenter.ltcreikiclockv3.util.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModel
import com.learnteachcenter.ltcreikiclockv3.viewmodel.AllPositionsViewModelFactory
import kotlinx.android.synthetic.main.activity_all_positions.*
import kotlinx.android.synthetic.main.list_item_position.view.*

class AllPositionsActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var reikiId: String
    private lateinit var reikiTitle: String

    private lateinit var timerState: ReikiSession.TimerState
    private var currentPosIndex: Int = -1
    private var previousPosIndex: Int = -1

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
            subscribeToReikiAndAllPositions()

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

    private fun subscribeToReikiAndAllPositions() {
        viewModel.reikiAndAllPositions.observe(this, Observer<ReikiAndAllPositions> {
            adapter.setPositions(it?.positions)
            viewModel.initSession(it!!)
            subscribeToReikiSession()
            setUpListeners()
        })
    }

    private fun subscribeToReikiSession() {
        viewModel.reikiSession.timeLeftObservable.observe(this, Observer<String> {

            try {
                val holder: PositionsAdapter.ViewHolder =
                    positionsRecyclerView.findViewHolderForAdapterPosition(currentPosIndex)
                            as PositionsAdapter.ViewHolder

                holder.itemView.duration.text = it
            }
            catch(exception: Exception) {}

            Log.d("Reiki", "Time remaining: $it")

        })
        viewModel.reikiSession.timerStateObservable.observe(this, Observer {

            timerState = it!!
            updateUI()

            Log.d("Reiki", "Timer state is $timerState")
        })
        viewModel.reikiSession.currentIndexObservable.observe(this, Observer {

            previousPosIndex = currentPosIndex
            currentPosIndex = it!!
            resetPreviousPositionUI(previousPosIndex)

            updatePositionUI(
                currentPosIndex,
                true,
                resources.getColor(R.color.colorHighlight),
                ContextCompat.getDrawable(this, R.drawable.ic_pause), null
            )

            Log.d("Reiki", "CurrentPosIndex is $currentPosIndex, Previous is $previousPosIndex")
        })
    }

    private fun updateUI() {
        when(timerState) {
            TimerState.Stopped -> {
                resetPreviousPositionUI(previousPosIndex)
                changeToPlayButton()
                showAddButton()
            }
            TimerState.Running -> {
                changeToPauseButton()
                hideAddButton()
            }
            TimerState.Paused -> {
                changeToPlayButton()
                hideAddButton()
            }
        }
    }

    private fun resetPreviousPositionUI(position: Int) {
        // Reset background color and image of previous Position item
        updatePositionUI(
            position,
            true,
            0,
            ContextCompat.getDrawable(this, R.drawable.ic_play_circle), null
        )
    }

    /**
     * Set View for each Position, which is a RecyclerView item
     * @param position  Position to set view
     * @param color     color to set
     * @param image     play/pause image to set
     * @param duration  duration text to set
     */
    private fun updatePositionUI(position: Int, changeColor: Boolean, color: Int, image: Drawable?, duration: String?) {

        // Getting a RecyclerView item, which is out of view, will throw NullPointerException.
        // So, use try/catch
        try {
            val holder: PositionsAdapter.ViewHolder =
                positionsRecyclerView.findViewHolderForAdapterPosition(position) as PositionsAdapter.ViewHolder

            if (changeColor) {
                holder.itemView.setBackgroundColor(color)
            }

            if (image != null) {
                holder.itemView.icon_play_pause.setImageDrawable(image)
            }

            if (duration != null) {
                holder.itemView.duration.text = duration
            }
        } catch (ex: Exception) {
            Log.wtf("DEBUG", "Exception in updatePositionUI: $ex")
        }
    }

    private fun setUpListeners() {

        fab_play_pause.setOnClickListener {

            when(timerState) {
                TimerState.Stopped -> {
                    viewModel.startSession(0)
                }
                TimerState.Running -> {
                    viewModel.pauseSession()
                }
                TimerState.Paused -> {
                    viewModel.resumeSession()
                }
            }

            hideAddButton()
        }

        fab_stop.setOnClickListener {
            viewModel.stopSession()
        }

        fab_add.setOnClickListener {
            onAddClick()
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