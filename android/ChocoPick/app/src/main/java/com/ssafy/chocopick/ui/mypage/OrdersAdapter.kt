package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.OrderWithStore
import com.ssafy.chocopick.databinding.ItemOrderBinding
import java.text.DecimalFormat


class OrdersAdapter(
    private val onClick: (OrderWithStore) -> Unit
) : ListAdapter<OrderWithStore, OrdersAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemOrderBinding,
        private val onClick: (OrderWithStore) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderWithStore) = with(binding) {
            tvStore.text = item.storeName.ifBlank { item.storeId }
            tvStatus.text = item.order.status

            tvDate.text = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.KOREA)
                .format(java.util.Date(item.order.orderDate))

//            tvSummary.text = item.order.items
//                .joinToString(" + ") { "${it.name} x${it.quantity}" }
//                .ifBlank { "주문 상품 없음" }
            tvSummary.text = when (item.order.items.size) {
                0 -> "주문 상품 없음"
                1 -> item.order.items.first().name
                else -> {
                    val first = item.order.items.first().name
                    val rest = item.order.items.size - 1
                    "$first 외 ${rest}개"
                }
            }

            tvPrice.text = "₩ ${DecimalFormat("#,###").format(item.order.totalPrice)}"

            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<OrderWithStore>() {
            override fun areItemsTheSame(oldItem: OrderWithStore, newItem: OrderWithStore) =
                oldItem.orderId == newItem.orderId

            override fun areContentsTheSame(oldItem: OrderWithStore, newItem: OrderWithStore) =
                oldItem == newItem
        }
    }
}


