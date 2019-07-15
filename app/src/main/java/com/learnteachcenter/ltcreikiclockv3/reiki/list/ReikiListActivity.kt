package com.learnteachcenter.ltcreikiclockv3.reiki.list

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import android.view.View.GONE
import android.view.View.VISIBLE
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.UpdateReikisOrderResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.util.ResourceObserver
import com.learnteachcenter.ltcreikiclockv3.reiki.positionlist.PositionListActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.edit.EditReikiActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.add.AddReikiActivity
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// https://stackoverflow.com/questions/38340358/how-to-enable-and-disable-drag-and-drop-on-a-recyclerview

class ReikiListActivity : AppCompatActivity() {

    private val TAG = "Reiki"
    private var mode: Mode = Mode.VIEW
    private lateinit var viewModel: ReikisViewModel
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable
    private var itemTouchHelper: ItemTouchHelper? = null
    private val reikiApi = Injection.provideReikiApi()

    private val adapter = ReikisAdapter(
        mutableListOf(),
        Mode.VIEW,
        clickListener = { reiki -> reikiItemClicked(reiki) },
        editListener = { reiki -> onEditReiki(reiki) },
        deleteListener = { reiki -> onDeleteReiki(reiki) },
        dragListener = { viewHolder -> onDragReiki(viewHolder) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.app_name).toUpperCase()

        viewModel = ViewModelProviders.of(this).get(ReikisViewModel::class.java)

        swipeBackground = ColorDrawable(ContextCompat.getColor(this, R.color.colorSwipeBackground))
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!

        initRecyclerView()
        subscribeObservers()

        // Show/hide Add button based on internet connectivity
        if(NetworkUtil.isConnected(this)) {
            fab.setOnClickListener {
                startActivity(Intent(this, AddReikiActivity::class.java))
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

    private fun reorderReikis() {
        // Update the seq no's on the server and the local database

        // TODO: update only if user has reordered.

        val reikis = adapter.getReikis()

        // Server

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

        // Local database
        val reikisToUpdate = Arrays.copyOfRange(reikis.toTypedArray(), 0, reikis.size)
        viewModel.updateReikis(*reikisToUpdate)

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
            LEFT or RIGHT) {

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
        itemTouchHelper!!.attachToRecyclerView(reikisRecyclerView)
    }

    fun onDragReiki(viewHolder: RecyclerView.ViewHolder) {
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
        val i = Intent(this, EditReikiActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_SEQ_NO, reiki.seqNo)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)

        startActivity(i)
    }

    fun onDeleteReiki(reiki: Reiki) {
        viewModel.deleteReiki(reiki.id)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if(mode == Mode.VIEW) {
            menu.findItem(R.id.action_edit).setVisible(true)
            menu.findItem(R.id.action_done).setVisible(false)
        } else {
            menu.findItem(R.id.action_edit).setVisible(false)
            menu.findItem(R.id.action_done).setVisible(true)
        }

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
                reorderReikis()
                changeToViewUI()
                true
            }
            R.id.action_logout -> {
                AuthenticationPrefs.clearAuthToken()
                viewModel.deleteReikis()
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

    enum class Mode {
        VIEW, EDIT
    }
}