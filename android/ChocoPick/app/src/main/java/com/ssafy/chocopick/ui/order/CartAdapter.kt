package com.ssafy.chocopick.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.databinding.ItemCartBinding

class CartAdapter(
    private val onPlus: (String) -> Unit,
    private val onMinus: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val items = mutableListOf<CartItem>()

    fun submitList(list: List<CartItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvName.text = item.name
            binding.tvUnitPrice.text = "단가: ${item.price}원"
            binding.tvQty.text = item.quantity.toString()
            binding.tvLinePrice.text = "소계: ${item.price * item.quantity}원"

            if (item.imageUrl.isNotBlank()) {
                Glide.with(binding.ivThumb)
                    .load(item.imageUrl)
                    .into(binding.ivThumb)
            } else {
                binding.ivThumb.setImageDrawable(null)
            }

            binding.btnPlus.setOnClickListener { onPlus(item.productId) }
            binding.btnMinus.setOnClickListener { onMinus(item.productId) }
            binding.btnRemove.setOnClickListener { onRemove(item.productId) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}