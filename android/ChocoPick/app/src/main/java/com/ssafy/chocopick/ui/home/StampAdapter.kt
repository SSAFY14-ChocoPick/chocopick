package com.ssafy.chocopick.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.ItemStampBinding
import kotlin.math.min

class StampAdapter(
    private val total: Int = 10,
    filled: Int
) : RecyclerView.Adapter<StampAdapter.VH>() {

    private var filledCount: Int = min(filled, total)

    inner class VH(private val binding: ItemStampBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val isFilled = position < filledCount
            binding.ivStamp.setImageResource(
                if (isFilled) R.drawable.ic_stamp_full else R.drawable.ic_stamp_empty
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = total

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }

    fun updateFilled(newCount: Int) {
        filledCount = min(newCount, total)
        notifyDataSetChanged()
    }
}
