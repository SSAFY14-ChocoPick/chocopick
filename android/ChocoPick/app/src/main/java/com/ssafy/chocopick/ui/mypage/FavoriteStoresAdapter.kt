package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.data.source.firebase.realtime.StoreWithId
import com.ssafy.chocopick.databinding.ItemStoreBinding

class FavoriteStoresAdapter(
    private val onClick: (StoreWithId) -> Unit
) : ListAdapter<StoreWithId, FavoriteStoresAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemStoreBinding,
        private val onClick: (StoreWithId) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StoreWithId) = with(binding) {
            tvStoreName.text = item.store.name
            tvStoreAddr.text = item.store.address
            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<StoreWithId>() {
            override fun areItemsTheSame(oldItem: StoreWithId, newItem: StoreWithId) =
                oldItem.storeId == newItem.storeId

            override fun areContentsTheSame(oldItem: StoreWithId, newItem: StoreWithId) =
                oldItem == newItem
        }
    }
}
