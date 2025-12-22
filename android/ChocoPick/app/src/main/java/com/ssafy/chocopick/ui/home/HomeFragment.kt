package com.ssafy.chocopick.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentHomeBinding
import com.ssafy.chocopick.ui.common.CurrentUserViewModel
import com.ssafy.chocopick.ui.common.CurrentUserViewModelFactory
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeVM: HomeViewModel by viewModels { HomeViewModelFactory() }

    // ✅ FirebaseAuth 직접 접근 대신 CurrentUserViewModel 사용
    private val currentUserVM: CurrentUserViewModel by viewModels { CurrentUserViewModelFactory() }

    private lateinit var recommendAdapter: RecommendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRecommend()
        collectRecommend()
        collectCurrentUser()

        // ✅ 유저 로드 (nickname 가져오려고)
        currentUserVM.loadMe()

        // ✅ 추천 로드
        homeVM.loadRecommendTop4()
    }

    private fun collectCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                currentUserVM.userState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val nickname = state.data.nickname.takeIf { it.isNotBlank() } ?: "회원"
                            binding.tvWelcome.text = "${nickname}님, 환영합니다 👋"
                        }
                        is UiState.Error -> {
                            // 로그인 안 됐거나 유저 정보 없을 때 fallback
                            binding.tvWelcome.text = "로딩중..."
                        }
                        else -> { /* Idle/Loading은 그대로 두기 */ }
                    }
                }
            }
        }
    }

    private fun setupRecommend() {
        recommendAdapter = RecommendAdapter { item ->
            // TODO: 상세로 이동 (productId 전달)
            // findNavController().navigate(R.id.productDetailFragment, bundleOf("productId" to item.productId))
        }

        binding.rvRecommend.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommend.adapter = recommendAdapter
    }

    private fun collectRecommend() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeVM.recommendState.collect { state ->
                    when (state) {
                        is UiState.Success -> recommendAdapter.submitList(state.data)
                        is UiState.Error -> {
                            // TODO: 토스트/스낵바 처리 원하면 여기
                        }
                        else -> {}
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
