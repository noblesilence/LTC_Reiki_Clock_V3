package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import kotlinx.android.synthetic.main.list_item_position.view.*

class PositionsAdapter (private val positions: MutableList<Position>)
    : RecyclerView.Adapter<PositionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_position))
    }

    override fun getItemCount(): Int = positions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(positions[position])
    }

    fun setPositions(positions: List<Position>?) {
        this.positions.clear()
        this.positions.addAll(positions!!)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var position: Position

        fun bind(position: Position) {
            this.position = position

            itemView.title.text = position.title
            itemView.duration.text = position.duration
        }
    }
}