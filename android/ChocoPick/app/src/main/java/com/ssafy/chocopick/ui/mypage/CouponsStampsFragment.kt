package com.ssafy.chocopick.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.databinding.FragmentCouponsStampsBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class CouponsStampsFragment : Fragment() {

    private var _binding: FragmentCouponsStampsBinding? = null
    private val binding get() = _binding!!

    private val vm: CouponsStampsViewModel by viewModels {
        CouponsStampsViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponsStampsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 뒤로가기(ImageView)
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ 쿠폰 사용하기 버튼 (일단 “쿠폰 화면에서의 동작” 정의)
        binding.btnUseCoupon.setOnClickListener {
            vm.useCouponIfPossible(
                onNotEnough = { Toast.makeText(requireContext(), "사용 가능한 쿠폰이 없어요.", Toast.LENGTH_SHORT).show() },
                onUsed = { Toast.makeText(requireContext(), "쿠폰을 사용했어요!", Toast.LENGTH_SHORT).show() }
            )
        }

        collectStates()

        // ✅ 로드 시작
        vm.load()
    }

    private fun collectStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1) 스탬프/등급(Reward)
                launch {
                    vm.rewardState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val reward = state.data
                                binding.tvStampCount.text = "현재 스탬프: ${reward.stamps}"
                            }
                            is UiState.Error -> {
                                binding.tvStampCount.text = "현재 스탬프: -"
                            }
                            else -> Unit
                        }
                    }
                }

                // 2) 쿠폰 목록
                launch {
                    vm.couponsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val coupons = state.data
                                binding.tvCouponEmpty.visibility =
                                    if (coupons.isEmpty()) View.VISIBLE else View.GONE

                                // 여기서 나중에 RecyclerView 붙이면 됨
                                binding.btnUseCoupon.isEnabled = coupons.isNotEmpty()
                            }
                            is UiState.Error -> {
                                binding.tvCouponEmpty.visibility = View.VISIBLE
                                binding.btnUseCoupon.isEnabled = false
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
