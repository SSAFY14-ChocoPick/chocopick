package com.ssafy.chocopick.ui.review

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.databinding.DialogReviewBinding
import kotlin.math.round

class ReviewEditDialogFragment : DialogFragment() {

    data class Args(
        val productId: String,
        val myUid: String,
        val myNickname: String,
        val existing: Review? = null
    )

    private lateinit var args: Args
    private var onSubmit: ((Review) -> Unit)? = null

    companion object {
        fun newInstance(args: Args, onSubmit: (Review) -> Unit): ReviewEditDialogFragment {
            return ReviewEditDialogFragment().apply {
                this.args = args
                this.onSubmit = onSubmit
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = DialogReviewBinding.inflate(LayoutInflater.from(requireContext()))

        val existing = args.existing
        if (existing != null) {
            b.rbInput.rating = existing.rating.toFloat()
            b.etContent.setText(existing.content)
        } else {
            b.rbInput.rating = 5.0f
        }

        fun normalizeHalfStep(v: Float): Double {
            val clamped = v.coerceIn(0f, 5f)
            val half = round(clamped * 2f) / 2f
            return half.toDouble()
        }

        b.tvRating.text = String.format("%.1f", normalizeHalfStep(b.rbInput.rating))

        b.rbInput.setOnRatingBarChangeListener { _, rating, _ ->
            val fixed = normalizeHalfStep(rating)
            b.tvRating.text = String.format("%.1f", fixed)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "리뷰 작성" else "리뷰 수정")
            .setView(b.root)
            .setPositiveButton("저장") { _, _ ->
                val content = b.etContent.text?.toString().orEmpty().trim()
                val rating: Float = normalizeHalfStep(b.rbInput.rating).toFloat()

                val now = System.currentTimeMillis()

                val newReview = if (existing == null) {
                    Review(
                        reviewId = "",
                        productId = args.productId,
                        uid = args.myUid,
                        nickname = args.myNickname,
                        rating = rating,
                        content = content,
                        createdAt = now,
                        updatedAt = now
                    )
                } else {
                    existing.copy(
                        rating = rating,
                        content = content,
                        updatedAt = now
                    )
                }

                onSubmit?.invoke(newReview)
            }
            .setNegativeButton("취소", null)
            .create()
    }
}