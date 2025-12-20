package com.ssafy.chocopick.ui.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(
    private val myUid: String,
    private val onEdit: (Review) -> Unit,
    private val onDelete: (Review) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Review, ReviewAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemReviewBinding) : RecyclerView.ViewHolder(b.root) {

        init {
            b.btnEdit.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                onEdit(getItem(pos))
            }

            b.btnDelete.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                onDelete(getItem(pos))
            }
        }

        fun bind(r: Review) {
            b.tvNickname.text = r.nickname
            b.tvContent.text = r.content
            b.ratingBar.rating = r.rating

            b.tvCreatedAt.text = formatTime(r.createdAt)

            val isMine = r.uid == myUid
            b.btnEdit.visibility = if (isMine) View.VISIBLE else View.GONE
            b.btnDelete.visibility = if (isMine) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(old: Review, new: Review) = old.reviewId == new.reviewId
            override fun areContentsTheSame(old: Review, new: Review) = old == new
        }

        private fun formatTime(epochMillis: Long): String {
            if (epochMillis <= 0L) return ""
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
            return sdf.format(Date(epochMillis))
        }
    }
}