package com.learnteachcenter.ltcreikiclockv3.position.list

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode
import com.learnteachcenter.ltcreikiclockv3.util.ListViewMode.*
import kotlinx.android.synthetic.main.list_item_position.view.*
import kotlinx.android.synthetic.main.list_item_position.view.imv_drag_handle
import kotlinx.android.synthetic.main.list_item_position.view.imv_edit
import kotlinx.android.synthetic.main.list_item_position.view.title
import java.util.*

class PositionsAdapter (private val positions: MutableList<Position>,
                        private var mode: ListViewMode,
                        private val playListener: (Int) -> Unit,
                        private val selectListener: (Position, Int) -> Unit,
                        private val editListener: (Position) -> Unit,
                        private val dragListener: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<PositionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewHolder =
            ViewHolder(parent.inflate(R.layout.list_item_position))

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

    fun updateViewMode(newMode: ListViewMode) {
        mode = newMode
    }

    override fun getItemCount(): Int = positions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(positions[position], playListener, selectListener, editListener, mode)
    }

    fun setPositions(positions: List<Position>?) {
        this.positions.clear()
        this.positions.addAll(positions!!)
        notifyDataSetChanged()
    }

    fun getPositions() = this.positions

    fun swapItems(from: Int, to: Int) {
        Collections.swap(positions, from, to)
        notifyItemMoved(from, to)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var position: Position

        fun bind(position: Position,
                 playListener: (Int) -> Unit,
                 selectListener: (Position, Int) -> Unit,
                 editListener: (Position) -> Unit,
                 mode: ListViewMode
        ) {

            this.position = position

            itemView.title.text = position.title
            itemView.duration.text = position.duration

            when(mode) {
                VIEW -> {
                    itemView.imv_edit.visibility = View.GONE
                    itemView.ckb_delete.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE
                }
                EDIT -> {
                    itemView.imv_edit.visibility = View.VISIBLE
                    itemView.imv_edit.setOnClickListener {
                        editListener(position)
                    }

                    itemView.ckb_delete.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE
                }
                DELETE -> {
                    itemView.ckb_delete.visibility = View.VISIBLE
                    itemView.ckb_delete.setOnClickListener {
                        selectListener(position, adapterPosition)
                    }

                    itemView.imv_edit.visibility = View.GONE
                    itemView.imv_drag_handle.visibility = View.GONE
                }
                REORDER -> {
                    itemView.imv_drag_handle.visibility = View.VISIBLE

                    itemView.imv_edit.visibility = View.GONE
                    itemView.ckb_delete.visibility = View.GONE
                }
            }
        }
    }
}