package com.ssafy.chocopick.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.ItemStampBinding
import kotlin.math.min

class StampAdapter(
    private val total: Int = 10,
    filled: Int
) : RecyclerView.Adapter<StampAdapter.VH>() {

    private var filledCount = min(filled, total)

    inner class VH(private val binding: ItemStampBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val isFilled = position < filledCount

            if (isFilled) {
                binding.root.background =
                    binding.root.context.getDrawable(R.drawable.bg_stamp_filled)
                binding.ivStamp.visibility = View.VISIBLE
            } else {
                binding.root.background =
                    binding.root.context.getDrawable(R.drawable.bg_stamp_empty)
                binding.ivStamp.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            ItemStampBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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

