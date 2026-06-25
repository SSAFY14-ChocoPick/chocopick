package com.ssafy.chocopick.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.ItemRecommendBinding

class RecommendAdapter(
    private val onClick: (RecommendUi) -> Unit
) : ListAdapter<RecommendUi, RecommendAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemRecommendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecommendUi) {
            binding.tvName.text = item.name

            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.chocopick_logo)
                .error(R.drawable.chocopick_logo)
                .into(binding.ivProduct)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRecommendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecommendUi>() {
            override fun areItemsTheSame(old: RecommendUi, new: RecommendUi) =
                old.productId == new.productId

            override fun areContentsTheSame(old: RecommendUi, new: RecommendUi) = old == new
        }
    }
}
