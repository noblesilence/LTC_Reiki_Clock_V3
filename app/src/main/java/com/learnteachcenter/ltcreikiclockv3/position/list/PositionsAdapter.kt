package com.learnteachcenter.ltcreikiclockv3.position.list

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.position.Position
import kotlinx.android.synthetic.main.list_item_position.view.*
import kotlinx.android.synthetic.main.list_item_position.view.imv_drag_handle
import kotlinx.android.synthetic.main.list_item_position.view.imv_edit
import kotlinx.android.synthetic.main.list_item_position.view.title
import java.util.*

class PositionsAdapter (private val positions: MutableList<Position>,
                        private var mode: PositionListActivity.Mode,
                        private val playListener: (Int) -> Unit,
                        private val editListener: (Position) -> Unit,
                        private val deleteListener: (Position) -> Unit,
                        private val dragListener: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<PositionsAdapter.ViewHolder>() {

    private lateinit var removedItem: Position
    private var removedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewHolder =
            ViewHolder(parent.inflate(R.layout.list_item_position))

        if(mode == PositionListActivity.Mode.EDIT) {
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

    fun updateViewMode(newMode: PositionListActivity.Mode) {
        mode = newMode
    }

    override fun getItemCount(): Int = positions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            positions[position],
            playListener,
            editListener,
            mode
        )
    }

    fun setPositions(positions: List<Position>?) {
        this.positions.clear()
        this.positions.addAll(positions!!)
        notifyDataSetChanged()
    }

    fun getPositions() = this.positions

    fun swapItems(from: Int, to: Int) {
        Collections.swap(positions, from, to)

        for(i in 0..positions.size - 1) {
            positions[i].seqNo = i
        }

        notifyItemMoved(from, to)
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPosition = viewHolder.adapterPosition
        removedItem = positions.get(viewHolder.adapterPosition)

        positions.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        Snackbar
            .make(viewHolder.itemView, "${removedItem.title} deleted.", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                positions.add(removedPosition, removedItem)
                notifyItemInserted(removedPosition)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    when(event) {
                        DISMISS_EVENT_TIMEOUT -> {
                            deleteListener(removedItem)
                        }
                    }

                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var position: Position

        fun bind(position: Position,
                 playListener: (Int) -> Unit,
                 editListener: (Position) -> Unit,
                 mode: PositionListActivity.Mode
        ) {

            this.position = position

            itemView.title.text = position.title
            itemView.duration.text = position.duration

            if(mode == PositionListActivity.Mode.VIEW) {
                itemView.imv_alarm.visibility = View.VISIBLE
                itemView.icon_play_pause.visibility = View.VISIBLE
                itemView.icon_play_pause.setOnClickListener{
                    playListener(this.adapterPosition)
                }

                itemView.imv_drag_handle.visibility = View.GONE
                itemView.imv_edit.visibility = View.GONE
                itemView.imv_edit.setOnClickListener(null)
            } else {
                itemView.imv_alarm.visibility = View.GONE
                itemView.icon_play_pause.visibility = View.GONE

                itemView.imv_drag_handle.visibility = View.VISIBLE
                itemView.imv_edit.visibility = View.VISIBLE
                itemView.imv_edit.setOnClickListener {
                    editListener(position)
                }
            }
        }
    }
}