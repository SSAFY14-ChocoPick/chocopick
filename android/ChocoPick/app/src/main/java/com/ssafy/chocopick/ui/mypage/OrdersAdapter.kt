package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.databinding.ItemOrderBinding
import java.text.DecimalFormat


class OrdersAdapter(
    private val onClick: (Order) -> Unit
) : ListAdapter<Order, OrdersAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemOrderBinding,
        private val onClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Order) = with(binding) {
            // 🔥 storeName이 없으니 일단 storeId 표시 (나중에 store 매핑하면 여기만 바꾸면 됨)
            tvStore.text = item.storeId.ifBlank { "매장" }

            tvStatus.text = item.status

            // createdAt → 날짜 문자열
            tvDate.text = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.KOREA)
                .format(java.util.Date(item.createdAt))

            tvPrice.text = "₩ ${DecimalFormat("#,###").format(item.totalPrice)}"

            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) =
                oldItem.orderId == newItem.orderId

            override fun areContentsTheSame(oldItem: Order, newItem: Order) =
                oldItem == newItem
        }
    }
}

