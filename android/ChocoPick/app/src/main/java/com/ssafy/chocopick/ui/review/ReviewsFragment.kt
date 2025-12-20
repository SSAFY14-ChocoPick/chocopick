package com.ssafy.chocopick.ui.review

import android.os.Bundle
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

        // ✅ back 구현
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
            // 또는 requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        collect()

        // ✅ 안전장치: productId 비면 바로 종료
        if (productId.isBlank()) {
            Toast.makeText(requireContext(), "productId가 비어있어요.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        vm.load(productId, myUid)

        binding.fabWrite.setOnClickListener {
            val dialog = ReviewEditDialogFragment.newInstance(
                ReviewEditDialogFragment.Args(
                    productId = productId,
                    myUid = myUid,
                    myNickname = myNickname,
                    existing = null
                )
            ) { review ->
                vm.upsert(review) {
                    Toast.makeText(requireContext(), "리뷰 저장 완료", Toast.LENGTH_SHORT).show()
                }
            }
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
        ) { review ->
            vm.upsert(review) {
                Toast.makeText(requireContext(), "리뷰 수정 완료", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(parentFragmentManager, "REVIEW_EDIT")
    }

    private fun confirmDelete(r: Review) {
        vm.delete(productId, r.reviewId, myUid) {
            Toast.makeText(requireContext(), "리뷰 삭제 완료", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.statsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val s = state.data
                                binding.tvAvg.text = String.format("%.1f", s.avgRating)
                                binding.tvCount.text = "(${s.reviewCount})"
                            }
                            else -> Unit
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}