package com.ssafy.chocopick.ui.review

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.databinding.DialogReviewBinding
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import kotlin.math.round

class ReviewEditDialogFragment : DialogFragment() {

    @Parcelize
    data class Args(
        val productId: String,
        val myUid: String,
        val myNickname: String,
        val existing: Review? = null
    ) : Parcelable

    @Parcelize
    data class Result(
        val review: Review
    ) : Parcelable

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments().getParcelable<Args>(KEY_ARGS)
            ?: run {
                dismiss()
                return super.onCreateDialog(savedInstanceState)
            }

        val b = DialogReviewBinding.inflate(LayoutInflater.from(requireContext()))

        val existing = args.existing
        if (existing != null) {
            b.tvTitle.text = "리뷰 수정"
            b.rbInput.rating = existing.rating
            b.etContent.setText(existing.content)
        } else {
            b.tvTitle.text = "리뷰 작성"
            b.rbInput.rating = 5.0f
        }

        fun normalizeHalfStep(v: Float): Float {
            val clamped = v.coerceIn(0f, 5f)
            val half = round(clamped * 2f) / 2f
            return half
        }

        fun updateRatingLabel() {
            val fixed = normalizeHalfStep(b.rbInput.rating)
            b.tvRating.text = String.format("%.1f", fixed)
        }

        updateRatingLabel()

        b.rbInput.setOnRatingBarChangeListener { _, rating, _ ->
            b.rbInput.rating = normalizeHalfStep(rating) // ✅ 0.5 단위로 강제 보정
            updateRatingLabel()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(b.root)
            .setPositiveButton("저장", null) // ✅ 여기서 null로 만들어두고 아래에서 커스텀 클릭
            .setNegativeButton("취소", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val content = b.etContent.text?.toString().orEmpty().trim()
                if (content.isBlank()) {
                    Toast.makeText(requireContext(), "리뷰 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val now = System.currentTimeMillis()
                val rating = normalizeHalfStep(b.rbInput.rating)

                val newReview = if (existing == null) {
                    Review(
                        reviewId = "",
                        productId = args.productId,
                        uid = args.myUid,
                        nickname = args.myNickname.trim(),
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

                parentFragmentManager.setFragmentResult(
                    REQ_KEY,
                    bundleOf(KEY_RESULT to Result(newReview))
                )
                dismiss()
            }
        }

        return dialog
    }

    companion object {
        const val REQ_KEY = "review_edit_result"
        private const val KEY_ARGS = "review_edit_args"
        private const val KEY_RESULT = "review_edit_payload"

        fun newInstance(args: Args): ReviewEditDialogFragment =
            ReviewEditDialogFragment().apply {
                arguments = bundleOf(KEY_ARGS to args)
            }

        fun readResult(bundle: Bundle): Review =
            bundle.getParcelable<Result>(KEY_RESULT)!!.review
    }
}