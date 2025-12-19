package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.databinding.ItemOrderBinding
import java.text.DecimalFormat

data class OrderListUi(
    val orderId: String,
    val storeName: String,
    val status: String,
    val dateText: String,
    val totalPrice: Int
)

class OrdersAdapter(
    private val onClick: (OrderListUi) -> Unit
) : ListAdapter<OrderListUi, OrdersAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemOrderBinding,
        private val onClick: (OrderListUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderListUi) = with(binding) {
            tvStore.text = item.storeName
            tvStatus.text = item.status
            tvDate.text = item.dateText
            tvPrice.text = "₩ ${DecimalFormat("#,###").format(item.totalPrice)}"

            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<OrderListUi>() {
            override fun areItemsTheSame(oldItem: OrderListUi, newItem: OrderListUi) =
                oldItem.orderId == newItem.orderId

            override fun areContentsTheSame(oldItem: OrderListUi, newItem: OrderListUi) =
                oldItem == newItem
        }
    }
}
