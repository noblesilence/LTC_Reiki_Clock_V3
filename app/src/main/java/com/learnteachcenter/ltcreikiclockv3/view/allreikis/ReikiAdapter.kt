package com.learnteachcenter.ltcreikiclockv3.view.allreikis

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import kotlinx.android.synthetic.main.list_item_reiki.view.*

class ReikiAdapter(private val reikis: MutableList<Reiki>)
    : RecyclerView.Adapter<ReikiAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_reiki))
    }

    override fun onBindViewHolder(holder: ReikiAdapter.ViewHolder, position: Int) {
        holder.bind(reikis[position])
    }

    override fun getItemCount(): Int = reikis.size

    fun updateReikis(reikis: List<Reiki>) {
        this.reikis.clear()
        this.reikis.addAll(reikis)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var reiki: Reiki

        fun bind(reiki: Reiki) {
            this.reiki = reiki

            println("title: ${reiki.title}")
            println("description: ${reiki.description}")

            itemView.title.text = reiki.title
            itemView.description.text = reiki.description
        }
    }
}