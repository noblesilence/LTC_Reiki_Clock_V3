package com.learnteachcenter.ltcreikiclockv3.reiki.all

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
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.database.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.util.ResourceObserver
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSessionActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.one.AddReikiActivity
import kotlinx.android.synthetic.main.activity_all_reikis.*
import kotlinx.android.synthetic.main.content_all_reikis.*

// https://stackoverflow.com/questions/38340358/how-to-enable-and-disable-drag-and-drop-on-a-recyclerview


class AllReikisActivity : AppCompatActivity() {

    private val TAG = "Reiki"

    private lateinit var viewModel: ReikisViewModel
    private lateinit var swipeBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    private var itemTouchHelper: ItemTouchHelper? = null

    private val adapter = ReikisAdapter(
        mutableListOf(),
        clickListener = { reiki -> reikiItemClicked(reiki) },
        deleteListener = { reiki -> onDeleteReiki(reiki) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_reikis)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Reiki Timer"

        viewModel = ViewModelProviders.of(this).get(ReikisViewModel::class.java)

        swipeBackground = ColorDrawable(resources.getColor(R.color.colorSwipeBackground))
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
            "AllReikisActivity",
            hideLoading = ::hideLoading,
            showLoading = ::showLoading,
            onSuccess = ::showReikis,
            onError = ::showErrorMessage
        )
        )
    }

    private fun showReikis(reikis: List<Reiki>) {
        adapter.setReikis(reikis)

        //changeToEditUI()
        changeToViewUI()
    }

    private fun changeToViewUI() {
        if(itemTouchHelper != null) {
            itemTouchHelper!!.attachToRecyclerView(null)
        }
    }

    private fun changeToEditUI() {
        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback (0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (adapter as ReikisAdapter).removeItem(viewHolder)
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

        val i = Intent(this, ReikiSessionActivity::class.java)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
        i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)

        startActivity(i)
    }

    fun onDeleteReiki(reiki: Reiki) {
        Log.wtf("Reiki", "Delete this Reiki: ${reiki.title}")

        viewModel.deleteReiki(reiki.id)
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
                viewModel.deleteReikis()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}