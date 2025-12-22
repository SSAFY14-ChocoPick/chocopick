package com.ssafy.chocopick.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentHomeBinding
import com.ssafy.chocopick.ui.common.CurrentUserViewModel
import com.ssafy.chocopick.ui.common.CurrentUserViewModelFactory
import com.ssafy.chocopick.ui.mypage.RewardFragment
import com.ssafy.chocopick.ui.order.ProductDetailFragment
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch
import kotlin.math.min

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeVM: HomeViewModel by viewModels { HomeViewModelFactory() }
    private val currentUserVM: CurrentUserViewModel by viewModels { CurrentUserViewModelFactory() }

    private lateinit var recommendAdapter: RecommendAdapter
    private lateinit var stampAdapter: StampAdapter

    private val stampTotal = 10
    private var rewardLoadedUid: String? = null

    // ✅ 배너
    private lateinit var bannerAdapter: BannerAdapter
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupStamp()
        setupRecommend()
        setupBanner()

        // ✅ 쿠폰함 버튼 → RewardFragment 이동
        binding.btnGoCoupons.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RewardFragment())
                .addToBackStack("REWARD")
                .commit()
        }

        collectCurrentUser()
        collectReward()
        collectRecommend()

        currentUserVM.loadMe()
        homeVM.loadRecommendTop4()
    }

    // -----------------------------
    // Stamp
    // -----------------------------
    private fun setupStamp() {
        stampAdapter = StampAdapter(total = stampTotal, filled = 0)
        binding.rvStamps.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = stampAdapter
        }
        binding.tvStampSummary.text = "스탬프 0/$stampTotal"
    }

    private fun collectCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                currentUserVM.userState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val user = state.data
                            val nickname = user.nickname.takeIf { it.isNotBlank() } ?: "회원"
                            binding.tvWelcome.text = "${nickname}님, 환영합니다 👋"

                            val uid = user.uid
                            if (uid.isNotBlank() && rewardLoadedUid != uid) {
                                rewardLoadedUid = uid
                                homeVM.loadReward(uid)
                            }
                            Log.d(TAG, "nickname=$nickname uid=$uid")
                        }

                        is UiState.Error -> {
                            binding.tvWelcome.text = "로딩중..."
                            binding.tvStampSummary.text = "스탬프 0/$stampTotal"
                            stampAdapter.updateFilled(0)
                            rewardLoadedUid = null
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun collectReward() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeVM.rewardState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val stamps = state.data.stamps
                            val display = min(stamps, stampTotal)
                            Log.d(TAG, "stamps=$stamps")

                            binding.tvStampSummary.text = "스탬프 $display/$stampTotal"
                            stampAdapter.updateFilled(stamps)
                        }

                        is UiState.Error -> {
                            binding.tvStampSummary.text = "스탬프 0/$stampTotal"
                            stampAdapter.updateFilled(0)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    // -----------------------------
    // Recommend
    // -----------------------------
    private fun setupRecommend() {
        recommendAdapter = RecommendAdapter { item ->
            // ✅ 추천 클릭 → ProductDetailFragment 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(item.productId))
                .addToBackStack("PRODUCT_DETAIL")
                .commit()
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
                        else -> {}
                    }
                }
            }
        }
    }

    // -----------------------------
    // Banner (ViewPager2)
    // -----------------------------
    private fun setupBanner() {
        val banners = listOf(
            BannerUi(
                imageRes = R.drawable.banner_1,
                title = "연말 한정 픽업 이벤트",
                desc = "지금 주문하면 스탬프 2배!"
            ),
            BannerUi(
                imageRes = R.drawable.banner_2,
                title = "오늘의 달콤 추천",
                desc = "새로 나온 초코 디저트를 만나보세요"
            )
        )

        bannerAdapter = BannerAdapter(banners) { item ->
            // TODO: 배너 클릭 이동 (원하면 여기서 연결)
        }

        binding.vpBanner.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 1
        }

        setupBannerIndicator(banners.size)
        bindBannerIndicatorWithPager(binding.vpBanner, banners.size)

        startAutoSlide(banners.size)
    }

    private fun setupBannerIndicator(count: Int) {
        binding.bannerIndicator.removeAllViews()
        repeat(count) { idx ->
            val dot = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_indicator_dot)
                isSelected = idx == 0
                val size = resources.getDimensionPixelSize(R.dimen.indicator_dot_size)
                val lp = ViewGroup.MarginLayoutParams(size, size).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.indicator_dot_margin)
                }
                layoutParams = lp
            }
            binding.bannerIndicator.addView(dot)
        }
    }

    private fun bindBannerIndicatorWithPager(pager: ViewPager2, count: Int) {
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until count) {
                    val dot = binding.bannerIndicator.getChildAt(i) as ImageView
                    dot.isSelected = (i == position)
                }
            }
        })
    }

    private fun startAutoSlide(count: Int) {
        stopAutoSlide()

        bannerRunnable = object : Runnable {
            override fun run() {
                if (_binding == null || count <= 1) return
                val next = (binding.vpBanner.currentItem + 1) % count
                binding.vpBanner.setCurrentItem(next, true)
                bannerHandler.postDelayed(this, 3500L)
            }
        }
        bannerHandler.postDelayed(bannerRunnable!!, 3500L)
    }

    private fun stopAutoSlide() {
        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }
        bannerRunnable = null
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) startAutoSlide(bannerAdapter.itemCount)
    }

    override fun onPause() {
        super.onPause()
        stopAutoSlide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoSlide()
        _binding = null
    }
}
