package com.learnteachcenter.ltcreikiclockv3.view.allreikis

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import kotlinx.android.synthetic.main.list_item_reiki.view.*

// https://www.andreasjakl.com/recyclerview-kotlin-style-click-listener-android/

class ReikiAdapter(private val reikis: MutableList<Reiki>, val clickListener: (Reiki) -> Unit)
    : RecyclerView.Adapter<ReikiAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_reiki))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reikis[position], clickListener)
    }

    override fun getItemCount(): Int = reikis.size

    fun updateReikis(reikis: List<Reiki>) {
        this.reikis.clear()
        this.reikis.addAll(reikis)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var reiki: Reiki

        fun bind(reiki: Reiki, clickListener: (Reiki) -> Unit) {
            this.reiki = reiki

            itemView.title.text = reiki.title
            itemView.description.text = reiki.description
            itemView.setOnClickListener { clickListener(reiki) }
        }
    }
}