package com.ssafy.chocopick.ui.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.databinding.ItemChatAiBinding
import com.ssafy.chocopick.databinding.ItemChatUserBinding

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }

    fun submit(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserVH(ItemChatUserBinding.inflate(inflater, parent, false))
        } else {
            AiVH(ItemChatAiBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        if (holder is UserVH) holder.bind(msg)
        if (holder is AiVH) holder.bind(msg)
    }

    class UserVH(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.root.findViewById<android.widget.TextView>(0).text = item.message
        }
    }

    class AiVH(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.root.findViewById<android.widget.TextView>(0).text = item.message
        }
    }
}
