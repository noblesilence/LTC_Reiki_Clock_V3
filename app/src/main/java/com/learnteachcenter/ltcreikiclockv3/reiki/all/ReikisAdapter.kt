package com.learnteachcenter.ltcreikiclockv3.reiki.all

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.inflate
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki
import kotlinx.android.synthetic.main.list_item_reiki.view.*

// https://www.andreasjakl.com/recyclerview-kotlin-style-click-listener-android/

class ReikisAdapter(private val reikis: MutableList<Reiki>, val clickListener: (Reiki) -> Unit)
    : RecyclerView.Adapter<ReikisAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_reiki))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reikis[position], clickListener)
    }

    override fun getItemCount(): Int = reikis.size

    fun setReikis(reikis: List<Reiki>) {
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