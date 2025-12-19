package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.databinding.ItemStoreFavoriteBinding

class AllStoresAdapter(
    private var favoriteIds: Set<String>,
    private val onToggleFavorite: (storeId: String, newValue: Boolean) -> Unit
) : RecyclerView.Adapter<AllStoresAdapter.VH>() {

    private val items = mutableListOf<Store>()

    fun submitList(stores: List<Store>, favoriteIds: Set<String>) {
        this.favoriteIds = favoriteIds
        items.clear()
        items.addAll(stores)
        notifyDataSetChanged()
    }

    inner class VH(private val binding: ItemStoreFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(store: Store) = with(binding) {
            tvStoreName.text = store.name
            tvStoreAddress.text = store.address

            val isFav = favoriteIds.contains(store.storeId)
            btnStar.setImageResource(if (isFav) R.drawable.ic_star_on else R.drawable.ic_star_off)

            btnStar.setOnClickListener {
                val newValue = !favoriteIds.contains(store.storeId)
                onToggleFavorite(store.storeId, newValue)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStoreFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
