package com.ssafy.chocopick.ui.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.databinding.ItemProductBinding

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val items  = mutableListOf<Product>()

    fun submitList(list: List<Product>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(item : Product){
            binding.tvName.text = item.name
            binding.tvPrice.text = "${item.price}원"

            if (item.imageUrl.isNotBlank()) {
                Glide.with(binding.ivThumb)
                    .load(item.imageUrl)
                    .into(binding.ivThumb)
            } else {
                binding.ivThumb.setImageDrawable(null)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductAdapter.ViewHolder, position: Int) {
        holder.bindItems(items[position])
    }

    override fun getItemCount(): Int = items.size

}