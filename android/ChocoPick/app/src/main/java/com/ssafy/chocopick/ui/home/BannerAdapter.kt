package com.ssafy.chocopick.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.R

class BannerAdapter(
    private val items: List<BannerUi>,
    private val onClick: ((BannerUi) -> Unit)? = null
) : RecyclerView.Adapter<BannerAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv = view.findViewById<ImageView>(R.id.ivBanner)
        private val title = view.findViewById<TextView>(R.id.tvBannerTitle)
        private val desc = view.findViewById<TextView>(R.id.tvBannerDesc)

        fun bind(item: BannerUi) {
            iv.setImageResource(item.imageRes)
            title.text = item.title
            desc.text = item.desc
            itemView.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
}
