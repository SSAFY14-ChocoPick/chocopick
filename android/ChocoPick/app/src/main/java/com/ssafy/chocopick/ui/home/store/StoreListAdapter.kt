package com.ssafy.chocopick.ui.home.store

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.databinding.ItemStoreBinding

class StoreListAdapter(
    private val onClick : (Store) -> Unit
) : RecyclerView.Adapter<StoreListAdapter.ViewHolder>() {



    val storeItems = mutableListOf<Store>()
    inner class ViewHolder(private val binding : ItemStoreBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindItems(store : Store){
            binding.tvStoreName.text = store.name
            binding.tvStoreAddr.text = store.address
            //binding.tvRegion.text = store.region.ifBlank { "지역 정보 없음" }

            binding.root.setOnClickListener { onClick(store) }
        }
    }

    fun submitList(list: List<Store>){
        storeItems.clear()
        storeItems.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreListAdapter.ViewHolder {

        val binding = ItemStoreBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreListAdapter.ViewHolder, position: Int) {
        holder.bindItems(storeItems[position])
    }

    override fun getItemCount(): Int {
        return storeItems.size
    }
}