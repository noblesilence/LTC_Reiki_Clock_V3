package com.learnteachcenter.ltcreikiclockv3.reiki.positionlist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.UpdatePositionsOrderResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession.State
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession.ReikiSessionEvent.NONE
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession.ReikiSessionEvent.STATE_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession.ReikiSessionEvent.INDEX_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession.ReikiSessionEvent.TIME_LEFT_CHANGED
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.position.AddPositionActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.position.EditPositionActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.reiki.reikisession.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_all_positions.*
import kotlinx.android.synthetic.main.list_item_position.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PositionListActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private var mode: Mode = Mode.VIEW

    private var itemTouchHelper: ItemTouchHelper? = null

    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    private val reikiApi = Injection.provideReikiApi()
    private val repository = Injection.provideReikiRepository()

    private lateinit var reikiId: String
    private lateinit var reikiTitle: String
    private lateinit var viewModel: PositionListViewModel
    private val adapter = PositionsAdapter(
        mutableListOf(),
        Mode.VIEW,
        editListener = { position -> onEditPosition(position) },
        deleteListener = { position -> onDeletePosition(position) },
        dragListener = { viewHolder -> onDragPosition(viewHolder) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.wtf("Reiki", "onCreate")

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.wtf("Reiki", "Portrait")
        } else {
            if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.wtf("Reiki", "Landscape")
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)
        configureUI()

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            reikiTitle = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)

            supportActionBar?.title = reikiTitle

            viewModel = ViewModelProviders.of(this,
                PositionListViewModelFactory(reikiId)
            ).get(PositionListViewModel::class.java)

            swipeBackground = ColorDrawable(ContextCompat.getColor(this, R.color.colorSwipeBackground))
            deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!

            initRecyclerView()
            subscribeToReikiAndAllPositions()
        }
    }

    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_chevron_left)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                onBackPressed()
                finish()
            }
        })
    }

    private fun initRecyclerView() {
        positionsRecyclerView.layoutManager = LinearLayoutManager(this)
        positionsRecyclerView.adapter = adapter
    }

    private fun subscribeToReikiAndAllPositions() {
        viewModel.reikiAndAllPositions.observe(this, Observer<ReikiAndAllPositions> {
            // Sort the Positions by seq no
            val sortedPositions = it?.positions!!.sortedWith(compareBy({ it.seqNo }))
            adapter.setPositions(sortedPositions)
            viewModel.initSession(it)
            subscribeToReikiSession()
            setUpListeners()
        })
    }

    private fun subscribeToReikiSession() {

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

                            Log.wtf("Reiki", "State is running. Change to pause button.")

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
                    updateButtons(reikiSession.getState()) // Need to add this to handle screen rotation
                }

                NONE -> {
                    // Do nothing
                }
            }
        })
    }

    private fun updateButtons(state: State) {
        when(state) {
            State.STOPPED -> {
                changeToPlayButton()
                fab_add_position.alpha = 1F
            }
            State.RUNNING -> {
                changeToPauseButton()
                fab_add_position.alpha = 0F
            }
            State.PAUSED -> {
                changeToPlayButton()
                fab_add_position.alpha = 0F
            }
        }
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
            Log.wtf("Reiki", "[PositionListActivity] highlightItem ->  itemIndex: $itemIndex, itemDuration: $itemDuration. \nException: $exception.")
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
            Log.wtf("Reiki", "[PositionListActivity] highlightItem -> Exception: $exception")
        }
    }

    private fun changeToPlayButton() {
        fab_play_pause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun changeToPauseButton() {
        fab_play_pause.setImageResource(R.drawable.ic_pause)
    }

    private fun reorderPositions() {
        // Update the Seq No's on the server and the local database

        // TODO: update only if order is changed

        val positions = adapter.getPositions()

        // Update in local database
        val positionsToUpdate = Arrays.copyOfRange(positions.toTypedArray(), 0, positions.size)
        repository.updatePositions(*positionsToUpdate)

        // Update on the server

        val call: Call<UpdatePositionsOrderResponse> = reikiApi.updatePositionsOrder(reikiId, positions)

        call.enqueue(object: Callback<UpdatePositionsOrderResponse> {
            override fun onFailure(call: Call<UpdatePositionsOrderResponse>, t: Throwable) {
                Log.wtf("Reiki", "Error updating on the server ${t.message}")
            }

            override fun onResponse(call: Call<UpdatePositionsOrderResponse>, response: Response<UpdatePositionsOrderResponse>) {
                val updateResponse: UpdatePositionsOrderResponse? = response.body()

                if(updateResponse != null) {
                    if(!updateResponse.success) {
                        val jObjError = JSONObject(response.errorBody()!!.string())

                        Log.wtf("Reiki", "Update error $jObjError")
                    }
                }
            }
        })

        adapter.updateViewMode(Mode.VIEW)
        adapter.notifyDataSetChanged()
    }

    private fun changeToViewUI() {
        if(itemTouchHelper != null) {
            itemTouchHelper!!.attachToRecyclerView(null)
        }
    }

    private fun changeToEditUI() {

        adapter.updateViewMode(Mode.EDIT)
        adapter.notifyDataSetChanged()

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback (
            UP or DOWN or START or END,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if(actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                adapter.swapItems(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                adapter.removeItem(viewHolder)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if(dX > 0) {
                    swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMargin )
                } else {
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(
                        itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin )
                }

                c.save()

                swipeBackground.draw(c)

                if(dX > 0) {
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                } else {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }

                deleteIcon.draw(c)

                c.restore()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper!!.attachToRecyclerView(positionsRecyclerView)
    }

    private fun onEditPosition(position: Position) {
        val i = Intent(this, EditPositionActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_POSITION_ID, position.id)
        i.putExtra(IntentExtraNames.EXTRA_POSITION_SEQ_NO, position.seqNo)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, position.reikiId)
        i.putExtra(IntentExtraNames.EXTRA_POSITION_TITLE, position.title)
        i.putExtra(IntentExtraNames.EXTRA_POSITION_DURATION, position.duration)

        startActivity(i)
    }

    fun onDeletePosition(position: Position) {
        viewModel.deletePosition(reikiId, position.id)
    }

    private fun onDragPosition(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if(mode == Mode.VIEW) {
            menu.findItem(R.id.action_edit).setVisible(true)
            menu.findItem(R.id.action_done).setVisible(false)
        } else {
            menu.findItem(R.id.action_edit).setVisible(false)
            menu.findItem(R.id.action_done).setVisible(true)
        }

        menu.findItem(R.id.action_logout).setVisible(false)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_edit -> {
                mode = Mode.EDIT
                invalidateOptionsMenu()
                changeToEditUI()
                true
            }
            R.id.action_done -> {
                mode = Mode.VIEW
                invalidateOptionsMenu()
                reorderPositions()
                changeToViewUI()
                true
            }
            R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        viewModel.stopSession()
        super.onBackPressed()
    }

    enum class Mode {
        VIEW, EDIT
    }
}