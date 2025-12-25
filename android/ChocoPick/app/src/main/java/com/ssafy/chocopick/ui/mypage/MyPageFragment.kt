package com.ssafy.chocopick.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentMyPageBinding
import com.ssafy.chocopick.ui.auth.AuthViewModel
import com.ssafy.chocopick.ui.auth.AuthViewModelFactory
import com.ssafy.chocopick.ui.auth.LoginActivity
import com.ssafy.chocopick.ui.order.ServiceLocator
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch
import java.text.DecimalFormat


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MyPageFragment"

class MyPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }
    private val myPageViewModel: MyPageViewModel by viewModels {
        MyPageViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectUserState()
        collectRewardState()
        collectRecentOrder()

        // 가장 최근 주문 1개 load
        Log.d(TAG, "loadRecentOrder()")
        myPageViewModel.loadRecentOrder()
        // 프로필 정보 load
        Log.d(TAG, "loadMyProfile()")
        myPageViewModel.loadMyProfile()
        // 리워드 정보 load
        Log.d(TAG, "loadReward()")
        myPageViewModel.loadReward()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            // 로그인 화면 이동
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            ServiceLocator.clearCartRepository()
            startActivity(intent)
        }

        // 1) 프로필 카드(내 정보 수정) -> EditProfileFragment
        binding.sectionProfile.setOnClickListener {
            navigate(EditProfileFragment(), "EDIT_PROFILE")
        }

        // 2) 최근 주문내역 전체보기 -> OrdersFragment
        binding.tvMoreOrders.setOnClickListener {
            navigate(OrdersFragment(), "ORDERS")
        }

        // 3) 가장 최근 주문 1개 클릭 -> OrderDetailFragment
        binding.orderItem1.setOnClickListener {
            recentOrderId?.let { orderId ->
                navigate(
                    OrderDetailFragment.newInstance(orderId),
                    "ORDER_DETAIL"
                )
            }
        }

        // 4) 내 메뉴: 쿠폰/스탬프
        binding.tvMenuCoupons.setOnClickListener {
            navigate(RewardFragment(), "COUPONS_STAMPS")
        }

        // 5) 내 메뉴: 즐겨찾는 매장
        binding.tvMenuFavorites.setOnClickListener {
            navigate(FavoriteStoresFragment(), "FAVORITE_STORES")
        }

        // 6) 내 메뉴: 알림 설정
        binding.tvMenuSettings.setOnClickListener {
            navigate(NotificationSettingsFragment(), "NOTIFICATION_SETTINGS")
        }



    }

    private fun navigate(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }


    private fun collectUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                myPageViewModel.userState.collect { state ->
                    when(state) {
                        is UiState.Idle -> {}
                        is UiState.Loading -> { /* 로딩 표시 원하면 */ }
                        is UiState.Success -> {
                            val nickname = state.data.nickname
                            binding.tvGreeting.text =
                                if (nickname.isBlank()) "회원님 안녕하세요"
                                else "${nickname}님 안녕하세요"
                        }
                        is UiState.Error -> {
                            binding.tvGreeting.text = "회원님 안녕하세요"
                        }
                    }
                }
            }
        }
    }

    private fun collectRewardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.rewardState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val reward = state.data

                            // 1️⃣ 등급 텍스트
                            binding.tvMembershipGrade.text = reward.membershipTier

                            // 2️⃣ progress 계산 (주문 누적수 기준)
                            val progressPercent = when (reward.membershipTier) {
                                "BRONZE" -> {
                                    // 0 ~ 9 → 0 ~ 100
                                    ((reward.totalOrders.coerceIn(0, 9) * 100) / 10)
                                }
                                "SILVER" -> {
                                    // 10 ~ 29 → 0 ~ 100
                                    (((reward.totalOrders - 10).coerceIn(0, 19) * 100) / 20)
                                }
                                "GOLD" -> 100
                                else -> 0
                            }

                            binding.progressToNext.progress = progressPercent

                            // 3️⃣ 혜택 설명
                            binding.tvMembershipDesc.text =
                                myPageViewModel.getBenefitText(reward.membershipTier)

                            // 4️⃣ 다음 등급 안내 문구
                            val nextLeft = when (reward.membershipTier) {
                                "BRONZE" -> (10 - reward.totalOrders).coerceAtLeast(0)
                                "SILVER" -> (30 - reward.totalOrders).coerceAtLeast(0)
                                else -> 0
                            }

                            binding.tvNextGradeHint.text =
                                if (reward.membershipTier == "GOLD")
                                    "최고 등급이에요 🎉"
                                else
                                    "다음 등급까지 ${nextLeft}건 남았어요"
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private var recentOrderId: String? = null


    private fun collectRecentOrder() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.recentOrderState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val orderWithStore = state.data
                            val order = orderWithStore.order
                            recentOrderId = order.orderId

                            binding.containerHasOrders.visibility = View.VISIBLE
                            binding.tvEmptyOrders.visibility = View.GONE

                            // ✅ storeId → storeName
                            binding.tvOrderStore1.text =
                                orderWithStore.storeName.ifBlank { orderWithStore.storeId }

                            binding.tvOrderStatus1.text = order.status

                            binding.tvOrderMenu1.text =
                                order.items.joinToString(", ") { it.name }

                            binding.tvOrderDate1.text =
                                myPageViewModel.formatOrderDate(order.orderDate)

                            binding.tvOrderPrice1.text =
                                "₩ ${DecimalFormat("#,###").format(order.totalPrice)}"
                        }

                        is UiState.Loading -> {
                            binding.containerHasOrders.visibility = View.GONE
                            binding.tvEmptyOrders.visibility = View.VISIBLE
                            // 로딩 UI가 있으면 여기서 처리 (없으면 무시 가능)
                        }

                        else -> {
                            binding.containerHasOrders.visibility = View.GONE
                            binding.tvEmptyOrders.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}