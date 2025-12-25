package com.ssafy.chocopick.ui.review

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.databinding.FragmentReviewsBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

private const val ARG_PRODUCT_ID = "productId"
private const val ARG_MY_NICKNAME = "myNickname"
private const val TAG = "ReviewsFragment"
class ReviewsFragment : Fragment(R.layout.fragment_reviews) {

    companion object {
        fun newInstance(productId: String, myNickname: String) =
            ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUCT_ID, productId)
                    putString(ARG_MY_NICKNAME, myNickname)
                }
            }
    }

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!

    private val productId: String by lazy { arguments?.getString(ARG_PRODUCT_ID).orEmpty() }
    private val myNickname: String by lazy { arguments?.getString(ARG_MY_NICKNAME).orEmpty() }
    private val myUid: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    private val vm: ReviewViewModel by viewModels { ReviewViewModelFactory() }

    private val adapter by lazy {
        ReviewAdapter(
            myUid = myUid,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewsBinding.bind(view)

        // ✅ back
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ Dialog 결과 수신(정석)
        parentFragmentManager.setFragmentResultListener(
            ReviewEditDialogFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val review = ReviewEditDialogFragment.readResult(bundle)
            Log.d("TAG", "review dialog에서 받아옴 $review")
            vm.upsert(review) {
                Toast.makeText(requireContext(), "리뷰 저장 완료", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        collect()

        if (productId.isBlank()) {
            Toast.makeText(requireContext(), "productId가 비어있어요.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        vm.load(productId, myUid)

        // 작성 버튼
        binding.fabWrite.setOnClickListener {
            Log.d("TAG", "review: fabWrite 누름 myUid: $myUid, myNickname: $myNickname")

            val dialog = ReviewEditDialogFragment.newInstance(
                ReviewEditDialogFragment.Args(
                    productId = productId,
                    myUid = myUid,
                    myNickname = myNickname,
                    existing = null
                )
            )
            dialog.show(parentFragmentManager, "REVIEW_WRITE")
        }
    }

    private fun showEditDialog(existing: Review) {
        val dialog = ReviewEditDialogFragment.newInstance(
            ReviewEditDialogFragment.Args(
                productId = productId,
                myUid = myUid,
                myNickname = myNickname,
                existing = existing
            )
        )
        dialog.show(parentFragmentManager, "REVIEW_EDIT")
    }

    private fun confirmDelete(r: Review) {
        vm.delete(productId, r.reviewId, myUid) {
            Toast.makeText(requireContext(), "리뷰 삭제 완료", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addChip(text: String) {
        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
            this.text = text
            isClickable = false
            isCheckable = false
        }
        binding.chipGroupSummary.addView(chip)
    }

    private fun collect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.statsState.collect { state ->
                        if (state is UiState.Success) {
                            val s = state.data
                            binding.tvAvg.text = String.format("%.1f", s.avgRating)
                            binding.tvCount.text = "(${s.reviewCount})"
                        }
                    }
                }

                launch {
                    vm.reviewsState.collect { state ->
                        when (state) {
                            is UiState.Success -> adapter.submitList(state.data)
                            is UiState.Error ->
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    vm.summaryState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressSummary.visibility = View.VISIBLE
                                binding.tvSummary.text = "리뷰 요약을 생성 중입니다..."
                                binding.chipGroupSummary.removeAllViews()
                            }

                            is UiState.Success -> {
                                binding.progressSummary.visibility = View.GONE
                                val s = state.data

                                // ✅ 장점/단점만 표시 (overall 제거)
                                binding.tvSummary.text = "장점: ${s.pros}\n단점: ${s.cons}"

                                binding.chipGroupSummary.removeAllViews()

                                // ✅ trend는 빼고 키워드만 (원하면 trend도 addChip 가능)
                                // addChip(s.trend)
                                s.keywords.forEach { addChip(it) }
                            }

                            is UiState.Error -> {
                                binding.progressSummary.visibility = View.GONE
                                binding.tvSummary.text = "요약을 불러오지 못했어요."
                                binding.chipGroupSummary.removeAllViews()
                            }

                            else -> Unit
                        }
                    }
                }




            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}