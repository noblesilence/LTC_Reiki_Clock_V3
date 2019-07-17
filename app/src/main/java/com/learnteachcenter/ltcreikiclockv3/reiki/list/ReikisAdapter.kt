package com.learnteachcenter.ltcreikiclockv3.reiki.list

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import kotlinx.android.synthetic.main.list_item_reiki.view.*
import android.view.MotionEvent
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode.*
import java.util.*

// https://www.andreasjakl.com/recyclerview-kotlin-style-click-listener-android/

class ReikisAdapter(private val reikis: MutableList<Reiki>,
                    private var mode: ListViewMode,
                    private val selectListener: (Reiki, Int) -> Unit,
                    private val clickListener: (Reiki) -> Unit,
                    private val editListener: (Reiki) -> Unit,
                    private val dragListener: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<ReikisAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(parent.inflate(R.layout.list_item_reiki))

        if(mode == EDIT) {
            viewHolder.itemView.imv_drag_handle.setOnTouchListener {
                    _, event ->

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
        holder.bind(reikis[position], selectListener, clickListener, editListener, mode)
    }

    override fun getItemCount(): Int = reikis.size

    fun updateViewMode(newMode: ListViewMode) {
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
        }

        notifyItemMoved(from, to)
    }

//    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
//        Log.wtf("Reiki", "[ReikisAdapter] (removeItem)")
//        removedPosition = viewHolder.adapterPosition
//        removedItem = reikis.get(viewHolder.adapterPosition)
//
//        reikis.removeAt(viewHolder.adapterPosition)
//        notifyItemRemoved(viewHolder.adapterPosition)
//
//        Snackbar
//            .make(viewHolder.itemView, "${removedItem.title} deleted.", Snackbar.LENGTH_LONG)
//            .setAction("UNDO") {
//                    reikis.add(removedPosition, removedItem)
//                    notifyItemInserted(removedPosition)
//                }
//            .addCallback(object : Snackbar.Callback() {
//                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
//                    when(event) {
//                        DISMISS_EVENT_TIMEOUT -> {
//                            Log.wtf("Reiki", "[ReikisAdapter] (removeItem) deleteListener")
//                            deleteListener(removedItem)
//                        }
//                    }
//                    super.onDismissed(transientBottomBar, event)
//                }
//            })
//            .show()
//    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var reiki: Reiki

        fun bind(reiki: Reiki, selectListener: (Reiki, Int) -> Unit, clickListener: (Reiki) -> Unit, editListener: (Reiki) -> Unit, mode: ListViewMode) {

            this.reiki = reiki

            itemView.title.text = reiki.title
            itemView.description.text = reiki.description

            when (mode) {
                VIEW -> {
                    itemView.description.visibility = View.VISIBLE
                    itemView.imv_arrow_right.visibility = View.VISIBLE

                    itemView.imv_edit.visibility = View.GONE
                    itemView.ckb_delete.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE

                    itemView.setOnClickListener { clickListener(reiki) }
                }
                EDIT -> {
                    itemView.imv_edit.visibility = View.VISIBLE
                    itemView.imv_edit.setOnClickListener {
                        editListener(reiki)
                    }

                    itemView.description.visibility = View.GONE
                    itemView.imv_arrow_right.visibility = View.GONE

                    itemView.ckb_delete.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE

                    itemView.setOnClickListener(null)
                }
                DELETE -> {
                    itemView.ckb_delete.visibility = View.VISIBLE
                    itemView.ckb_delete.setOnClickListener {
                        selectListener(reiki, adapterPosition)
                    }
                    itemView.description.visibility = View.GONE
                    itemView.imv_arrow_right.visibility = View.GONE

                    itemView.imv_edit.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE

                    itemView.setOnClickListener {
                        selectListener(reiki, adapterPosition)
                    }
                }
                REORDER -> {
                    itemView.imv_drag_handle.visibility = View.VISIBLE

                    itemView.description.visibility = View.GONE
                    itemView.imv_arrow_right.visibility = View.GONE

                    itemView.imv_edit.visibility = View.GONE
                    itemView.ckb_delete.visibility = View.GONE

                    itemView.setOnClickListener(null)
                }
            }
        }
    }
}