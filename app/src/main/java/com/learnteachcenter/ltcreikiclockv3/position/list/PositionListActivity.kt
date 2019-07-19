package com.learnteachcenter.ltcreikiclockv3.position.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
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
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession.State
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession.ReikiSessionEvent.NONE
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession.ReikiSessionEvent.STATE_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession.ReikiSessionEvent.INDEX_CHANGED
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession.ReikiSessionEvent.TIME_LEFT_CHANGED
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.position.add.AddPositionActivity
import com.learnteachcenter.ltcreikiclockv3.position.edit.EditPositionActivity
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.reikisession.ReikiSession
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode.*
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_all_positions.*
import kotlinx.android.synthetic.main.list_item_position.view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PositionListActivity : AppCompatActivity() {

    private var mode: ListViewMode = VIEW
    private val selectedItems: MutableList<Position> = mutableListOf()
    private var itemTouchHelper: ItemTouchHelper? = null

    private val reikiApi = Injection.provideReikiApi()
    private val repository = Injection.provideReikiRepository()

    private lateinit var reikiId: String
    private lateinit var reikiTitle: String
    private lateinit var viewModel: PositionListViewModel

    private var shouldReorder = false

    private val adapter = PositionsAdapter(
        mutableListOf(),
        VIEW,
        playListener = { position -> onPlayPosition(position) },
        selectListener = { position, itemIndex -> onSelectPosition(position, itemIndex) },
        editListener = { position -> onEditPosition(position) },
        dragListener = { viewHolder -> onDragPosition(viewHolder) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_positions)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            reikiTitle = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)

            viewModel = ViewModelProviders.of(this,
                PositionListViewModelFactory(reikiId)
            ).get(PositionListViewModel::class.java)

            configureUI()

            initRecyclerView()
            subscribeToReikiAndAllPositions()
        }
    }

    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = reikiTitle.toUpperCase()
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

    private fun updateSeqNums(positions: MutableList<Position>) : MutableList<Position> {
        var updatedList = mutableListOf<Position>()

        for(i in 0 until positions.size) {
            val position = positions.get(i)
            position.seqNo = i
            updatedList.add(position)
        }

        return updatedList
    }

    private fun reorderPositions() {
        // Update the Seq No's on the server and the local database

        var positions = adapter.getPositions()
        positions = updateSeqNums(positions)

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
    }

    private fun changeToViewUI() {
        adapter.updateViewMode(VIEW)
        adapter.notifyDataSetChanged()

        if(itemTouchHelper != null) {
            itemTouchHelper!!.attachToRecyclerView(null)
        }

        fab_add_position.show()
    }

    private fun changeToEditUI() {
        adapter.updateViewMode(EDIT)
        adapter.notifyDataSetChanged()
        fab_add_position.hide()

        val actionModeCallbacks: ActionMode.Callback = object: ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = menuInflater
                menuInflater.inflate(R.menu.toolbar_cab_edit, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                changeToViewUI()
            }
        }

        startSupportActionMode(actionModeCallbacks)
    }

    private fun changeToDeleteUI() {
        adapter.updateViewMode(DELETE)
        adapter.notifyDataSetChanged()
        fab_add_position.hide()

        val actionModeCallbacks: ActionMode.Callback = object: ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = menuInflater
                menuInflater.inflate(R.menu.toolbar_cab_delete, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                AlertDialog.Builder(this@PositionListActivity)
                    .setTitle("Confirm Delete")
                    .setMessage(getString(R.string.confirm_delete))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        onDeletePositions(*selectedItems.toTypedArray())
                        mode?.finish()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()

                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                selectedItems.clear()
                changeToViewUI()
            }
        }

        startSupportActionMode(actionModeCallbacks)
    }

    private fun onSelectPosition(position: Position, itemIndex: Int) {
        if(selectedItems.contains(position)) {
            selectedItems.remove(position)
            unCheckItem(itemIndex)
        } else {
            selectedItems.add(position)
            checkItem(itemIndex)
        }
    }

    private fun checkItem(itemIndex: Int) {
        try {
            val holder: PositionsAdapter.ViewHolder =
                    positionsRecyclerView.findViewHolderForAdapterPosition(itemIndex)
            as PositionsAdapter.ViewHolder

            holder.itemView.ckb_delete.isChecked = true
        } catch(exception: Exception) {}
    }

    private fun unCheckItem(itemIndex: Int) {
        try {
            val holder: PositionsAdapter.ViewHolder =
                    positionsRecyclerView.findViewHolderForAdapterPosition(itemIndex)
            as PositionsAdapter.ViewHolder

            holder.itemView.ckb_delete.isChecked = false
        } catch(exception: Exception) {}
    }

    private fun changeToReorderUI() {
        adapter.updateViewMode(REORDER)
        adapter.notifyDataSetChanged()
        fab_add_position.hide()

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback (
            UP or DOWN or START or END,
            0) {

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if(actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                adapter.swapItems(from, to)

                shouldReorder = true

                return true
            }
        }

        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper!!.attachToRecyclerView(positionsRecyclerView)

        val actionModeCallbacks: ActionMode.Callback = object: ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if(shouldReorder) {
                    reorderPositions()
                }

                mode?.finish()
                return true
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = menuInflater
                menuInflater.inflate(R.menu.toolbar_cab_reorder, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                shouldReorder = false
                changeToViewUI()
            }
        }

        startSupportActionMode(actionModeCallbacks)
    }

    private fun onPlayPosition(index: Int) {
        viewModel.startSession(index)
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

    private fun onDeletePositions(vararg positions: Position) {
        viewModel.deletePositions(reikiId, *positions)
    }

    private fun onDragPosition(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_logout).setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_edit -> {
                mode = EDIT
                invalidateOptionsMenu()
                changeToEditUI()
                true
            }
            R.id.action_delete -> {
                mode = DELETE
                changeToDeleteUI()
                true
            }
            R.id.action_reorder -> {
                mode = REORDER
                changeToReorderUI()
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
}