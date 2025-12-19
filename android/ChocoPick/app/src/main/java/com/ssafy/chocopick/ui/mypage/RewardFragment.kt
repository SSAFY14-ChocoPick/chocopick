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
import com.ssafy.chocopick.databinding.FragmentRewardBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch
import kotlin.math.min

class RewardFragment : Fragment() {

    private var _binding: FragmentRewardBinding? = null
    private val binding get() = _binding!!

    private val vm: RewardViewModel by viewModels { RewardViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ 아메리카노 쿠폰 발행(스탬프 10개)
        binding.btnIssueAmericano.setOnClickListener {
            vm.issueAmericano(
                onNotEnough = {
                    Toast.makeText(requireContext(), "스탬프가 10개 이상 필요해요.", Toast.LENGTH_SHORT).show()
                },
                onIssued = {
                    Toast.makeText(requireContext(), "아메리카노 쿠폰 1장을 발행했어요!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ✅ 아메리카노 쿠폰 사용
        binding.btnUseCoupon.setOnClickListener {
            vm.useCouponIfPossible(
                onNotEnough = {
                    Toast.makeText(requireContext(), "사용 가능한 쿠폰이 없어요.", Toast.LENGTH_SHORT).show()
                },
                onUsed = {
                    Toast.makeText(requireContext(), "쿠폰을 사용했어요!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        collectStates()

        // ✅ 최초 로드
        vm.load()
    }

    private fun collectStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.rewardState.collect { state ->
                        when (state) {
                            is UiState.Idle -> {
                                renderIdle()
                            }

                            is UiState.Loading -> {
                                renderLoading()
                            }

                            is UiState.Success -> {
                                val reward = state.data

                                // ✅ 멤버십
                                binding.tvTier.text = "멤버십: ${reward.membershipTier}"
                                binding.tvTierBenefit.text = vm.getBenefitText(reward.membershipTier)

                                // ✅ 스탬프 / 진행률(10개 단위)
                                binding.tvStampCount.text = "현재 스탬프: ${reward.stamps}"

                                val progressPercent = ((reward.stamps % 10) * 10) // 0~90
                                binding.progressStamp.progress = min(100, progressPercent)

                                // ✅ 발행 가능 여부
                                val canIssue = reward.stamps >= 10
                                binding.btnIssueAmericano.isEnabled = canIssue
                                binding.btnIssueAmericano.alpha = if (canIssue) 1f else 0.4f

                                val remain = if (reward.stamps % 10 == 0) 10 else (10 - (reward.stamps % 10))
                                binding.tvStampHint.text =
                                    if (canIssue) "쿠폰 발행이 가능해요! (스탬프 10개 사용)"
                                    else "10개 모으면 아메리카노 쿠폰 1장 발행 가능해요. (다음 발행까지 ${remain}개)"

                                // ✅ 보유 쿠폰
                                binding.tvAmericanoCount.text =
                                    "보유 아메리카노 쿠폰: ${reward.americanoCoupons}장"

                                val canUse = reward.americanoCoupons > 0
                                binding.btnUseCoupon.isEnabled = canUse
                                binding.btnUseCoupon.alpha = if (canUse) 1f else 0.4f
                            }

                            is UiState.Error -> {
                                renderError()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderIdle() = with(binding) {
        tvTier.text = "멤버십: -"
        tvTierBenefit.text = "이번 달 혜택: -"
        tvStampCount.text = "현재 스탬프: -"
        tvAmericanoCount.text = "보유 아메리카노 쿠폰: -장"
        progressStamp.progress = 0
        tvStampHint.text = "10개 모으면 아메리카노 쿠폰 1장 발행 가능해요."
        btnIssueAmericano.isEnabled = false
        btnIssueAmericano.alpha = 0.4f
        btnUseCoupon.isEnabled = false
        btnUseCoupon.alpha = 0.4f
    }

    private fun renderLoading() = with(binding) {
        // 로딩 중에는 화면을 "유지"하면서 버튼만 비활성화하면 UX가 자연스러움
        btnIssueAmericano.isEnabled = false
        btnIssueAmericano.alpha = 0.4f
        btnUseCoupon.isEnabled = false
        btnUseCoupon.alpha = 0.4f
    }

    private fun renderError() = with(binding) {
        tvTier.text = "멤버십: -"
        tvTierBenefit.text = "이번 달 혜택: -"
        tvStampCount.text = "현재 스탬프: -"
        tvAmericanoCount.text = "보유 아메리카노 쿠폰: -장"
        progressStamp.progress = 0
        tvStampHint.text = "정보를 불러오지 못했어요. 잠시 후 다시 시도해주세요."
        btnIssueAmericano.isEnabled = false
        btnIssueAmericano.alpha = 0.4f
        btnUseCoupon.isEnabled = false
        btnUseCoupon.alpha = 0.4f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
