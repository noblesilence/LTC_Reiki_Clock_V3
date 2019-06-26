package com.learnteachcenter.ltcreikiclockv3.reiki.all

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import kotlinx.android.synthetic.main.list_item_reiki.view.*
import android.util.Log
import android.view.MotionEvent
import java.util.*

// https://www.andreasjakl.com/recyclerview-kotlin-style-click-listener-android/

class ReikisAdapter(private val reikis: MutableList<Reiki>,
                    private var mode: AllReikisActivity.Mode,
                    private val clickListener: (Reiki) -> Unit,
                    private val editListener: (Reiki) -> Unit,
                    private val deleteListener: (Reiki) -> Unit,
                    private val dragListener: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<ReikisAdapter.ViewHolder>(){

    private lateinit var removedItem: Reiki
    private var removedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(parent.inflate(R.layout.list_item_reiki))

        if(mode == AllReikisActivity.Mode.EDIT) {
            viewHolder.itemView.imv_drag_handle.setOnTouchListener {
                    view, event ->

                if(event.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragListener(viewHolder)
                }

                return@setOnTouchListener true
            }
        } else {
            viewHolder.itemView.imv_drag_handle.setOnTouchListener(null)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reikis[position], clickListener, editListener, dragListener, mode)
    }

    override fun getItemCount(): Int = reikis.size

    fun updateViewMode(newMode: AllReikisActivity.Mode) {
        mode = newMode
    }

    fun setReikis(reikis: List<Reiki>) {
        this.reikis.clear()
        this.reikis.addAll(reikis)
        notifyDataSetChanged()
    }

    fun getReikis() = this.reikis

    fun swapItems(from: Int, to: Int) {
        Collections.swap(reikis, from, to)

        for(i in 0..reikis.size - 1) {
            reikis[i].seqNo = i

            Log.wtf("Reiki", "title: ${reikis[i].title}, seqNo: ${reikis[i].seqNo}")
        }

        notifyItemMoved(from, to)
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPosition = viewHolder.adapterPosition
        removedItem = reikis.get(viewHolder.adapterPosition)

        reikis.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        Snackbar
            .make(viewHolder.itemView, "${removedItem.title} deleted.", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                    reikis.add(removedPosition, removedItem)
                    notifyItemInserted(removedPosition)
                }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    when(event) {
                        Snackbar.Callback.DISMISS_EVENT_TIMEOUT -> {
                            deleteListener(removedItem)
                        }
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var reiki: Reiki

        fun bind(reiki: Reiki,
                 clickListener: (Reiki) -> Unit,
                 editListener: (Reiki) -> Unit,
                 dragListener: (ViewHolder) -> Unit,
                 mode: AllReikisActivity.Mode) {
            this.reiki = reiki

            itemView.title.text = reiki.title
            itemView.description.text = reiki.description

            if(mode == AllReikisActivity.Mode.VIEW) {
                itemView.imv_arrow_right.visibility = View.VISIBLE

                itemView.imv_drag_handle.visibility = View.GONE
                itemView.imv_edit.visibility = View.GONE

                // Remove Edit
                itemView.imv_edit.setOnClickListener(null)

                // Item Click
                itemView.setOnClickListener { clickListener(reiki) }
            } else {
                itemView.imv_drag_handle.visibility = View.VISIBLE
                itemView.imv_edit.visibility = View.VISIBLE

                itemView.imv_arrow_right.visibility = View.GONE

                // Edit
                itemView.imv_edit.setOnClickListener {
                    editListener(reiki)
                    Log.wtf("Reiki", "Should edit Reiki")
                }

                // remove the item onclick listener
                itemView.setOnClickListener(null)
            }
        }
    }
}