package com.learnteachcenter.ltcreikiclockv3.reiki.list

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.UpdateReikisOrderResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.util.ResourceObserver
import com.learnteachcenter.ltcreikiclockv3.position.list.PositionListActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.edit.EditReikiActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.add.AddReikiActivity
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.support.v7.view.ActionMode

class ReikiListActivity : AppCompatActivity() {

    companion object {
        var mActionMode: ActionMode? = null
    }

    private val TAG = "Reiki"
    private var mode: ListViewMode = ListViewMode.VIEW
    private val selectedItems: MutableList<Reiki> = mutableListOf()
    private lateinit var viewModel: ReikisViewModel
    private var itemTouchHelper: ItemTouchHelper? = null
    private val reikiApi = Injection.provideReikiApi()
    private var shouldReorder = false

    private val adapter = ReikisAdapter(
        mutableListOf(),
        ListViewMode.VIEW,
        selectListener = { reiki, itemIndex -> onSelectReiki(reiki, itemIndex) },
        clickListener = { reiki -> reikiItemClicked(reiki) },
        editListener = { reiki -> onEditReiki(reiki) },
        dragListener = { viewHolder -> onDragReiki(viewHolder) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)

        configureUI()

        viewModel = ViewModelProviders.of(this).get(ReikisViewModel::class.java)

        initRecyclerView()
        subscribeObservers()

        fab_add_reiki.setOnClickListener {
            startActivity(Intent(this, AddReikiActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        changeModeBasedOnConnectivity()
    }

    private fun changeModeBasedOnConnectivity() {
        // Show/hide Add button based on internet connectivity
        if(NetworkUtil.isConnected(this)) {
            fab_add_reiki.show()
            tvConnectivityNotice.visibility = GONE
        } else {
            fab_add_reiki.hide()
            tvConnectivityNotice.visibility = VISIBLE
        }

        invalidateOptionsMenu()
    }

    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.app_name).toUpperCase()
    }

    private fun initRecyclerView() {
        reikisRecyclerView.layoutManager = LinearLayoutManager(this)
        reikisRecyclerView.adapter = adapter
    }

    private fun subscribeObservers(){
        viewModel.reikis.observe(this, ResourceObserver(
            "ReikiListActivity",
            hideLoading = ::hideLoading,
            showLoading = ::showLoading,
            onSuccess = ::showReikis,
            onError = ::showErrorMessage
        )
        )
    }

    private fun showReikis(reikis: List<Reiki>) {
        adapter.setReikis(reikis)
        changeToViewUI()
    }

    private fun updateSeqNums(reikis: MutableList<Reiki>): MutableList<Reiki> {
        var updatedList = mutableListOf<Reiki>()

        for(i in 0 until reikis.size) {
            val reiki = reikis.get(i)
            reiki.seqNo = i
            updatedList.add(reiki)
        }

        return updatedList
    }

    // TODO: move this logic to repository class
    private fun reorderReikis() {
        // Update the seq no's on the server and the local database
        var reikis = adapter.getReikis()

        reikis = updateSeqNums(reikis)

        // Reorder Reikis in local database
        val reikisToUpdate = Arrays.copyOfRange(reikis.toTypedArray(), 0, reikis.size)
        viewModel.updateReikis(*reikisToUpdate)

        // Reorder Reikis on the server
        val call: Call<UpdateReikisOrderResponse> = reikiApi.updateReikisOrder(reikis)

        call.enqueue(object: Callback<UpdateReikisOrderResponse>{
            override fun onFailure(call: Call<UpdateReikisOrderResponse>, t: Throwable) {
                Log.wtf("Reiki", "Error updating on the server ${t.message}")
            }

            override fun onResponse(call: Call<UpdateReikisOrderResponse>, response: Response<UpdateReikisOrderResponse>) {
                val updateResponse: UpdateReikisOrderResponse? = response.body()

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
        adapter.updateViewMode(ListViewMode.VIEW)
        adapter.notifyDataSetChanged()

        if(itemTouchHelper != null) {
            itemTouchHelper!!.attachToRecyclerView(null)
        }

        changeModeBasedOnConnectivity()
    }

    private fun changeToEditUI() {
        adapter.updateViewMode(ListViewMode.EDIT)
        adapter.notifyDataSetChanged()
        fab_add_reiki.hide()

        val actionModeCallbacks: ActionMode.Callback = object: ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = menuInflater
                menuInflater.inflate(R.menu.toolbar_cab_edit, menu)
                mActionMode = mode
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                mActionMode = null
                changeToViewUI()
            }
        }

        startSupportActionMode(actionModeCallbacks)
    }

    private fun changeToDeleteUI() {
        adapter.updateViewMode(ListViewMode.DELETE)
        adapter.notifyDataSetChanged()
        fab_add_reiki.hide()

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

                AlertDialog.Builder(this@ReikiListActivity)
                    .setTitle("Confirm Delete")
                    .setMessage(getString(R.string.confirm_delete))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        onDeleteReikis(*selectedItems.toTypedArray())
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

    private fun onSelectReiki(reiki: Reiki, itemIndex: Int) {
        if (selectedItems.contains(reiki)) {
            selectedItems.remove(reiki)
        } else {
            selectedItems.add(reiki)
        }
    }

    private fun changeToReorderUI() {
        adapter.updateViewMode(ListViewMode.REORDER)
        adapter.notifyDataSetChanged()

        fab_add_reiki.hide()

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback (
            UP or DOWN or START or END, 0) {

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
        itemTouchHelper!!.attachToRecyclerView(reikisRecyclerView)

        val actionModeCallbacks: ActionMode.Callback = object: ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = menuInflater
                menuInflater.inflate(R.menu.toolbar_cab_reorder, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if(shouldReorder) {
                    reorderReikis()
                }

                mode?.finish()

                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                shouldReorder = false
                Log.wtf("Reiki", "Reset shouldReorder to false")
                changeToViewUI()
            }
        }

        startSupportActionMode(actionModeCallbacks)
    }

    private fun onDragReiki(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
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

        val i = Intent(this, PositionListActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)

        startActivity(i)
    }

    fun onEditReiki(reiki: Reiki) {

        if(mActionMode != null) {
            mActionMode?.finish()
        }

        val i = Intent(this, EditReikiActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_SEQ_NO, reiki.seqNo)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)

        startActivity(i)
    }

    fun onDeleteReikis(vararg reikis: Reiki) {
        viewModel.deleteReikis(*reikis)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if(!NetworkUtil.isConnected(this)) {
            menu.findItem(R.id.action_edit).setVisible(false)
            menu.findItem(R.id.action_delete).setVisible(false)
            menu.findItem(R.id.action_reorder).setVisible(false)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                mode = ListViewMode.EDIT
                changeToEditUI()
                true
            }
            R.id.action_delete -> {
                mode = ListViewMode.DELETE
                changeToDeleteUI()
                true
            }
            R.id.action_reorder -> {
                mode = ListViewMode.REORDER
                changeToReorderUI()
                true
            }
            R.id.action_logout -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}