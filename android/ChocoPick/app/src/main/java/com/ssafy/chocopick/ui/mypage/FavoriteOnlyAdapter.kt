package com.ssafy.chocopick.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.databinding.ItemStoreFavoriteBinding

class FavoriteOnlyAdapter(
    private val onRemoveFavorite: (storeId: String) -> Unit
) : RecyclerView.Adapter<FavoriteOnlyAdapter.VH>() {

    private val items = mutableListOf<Store>()

    fun submitList(stores: List<Store>) {
        items.clear()
        items.addAll(stores)
        notifyDataSetChanged()
    }

    inner class VH(private val binding: ItemStoreFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(store: Store) = with(binding) {
            tvStoreName.text = store.name
            tvStoreAddress.text = store.address

            // 즐겨찾기 목록이므로 기본은 ON
            btnStar.setImageResource(R.drawable.ic_star_on)

            btnStar.setOnClickListener {
                onRemoveFavorite(store.storeId)
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
