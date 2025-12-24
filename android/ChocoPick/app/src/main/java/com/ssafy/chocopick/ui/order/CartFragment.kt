//package com.ssafy.chocopick.ui.order
//
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import com.ssafy.chocopick.R
//import androidx.fragment.app.activityViewModels
//import androidx.fragment.app.replace
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.gson.Gson
//import com.ssafy.chocopick.data.model.CartItem
//import com.ssafy.chocopick.data.model.FcmRequestDto
//import com.ssafy.chocopick.data.remote.ApiProvider
//import com.ssafy.chocopick.data.source.firebase.realtime.RealtimeDbClient
//import com.ssafy.chocopick.databinding.FragmentCartBinding
//import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModel
//import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModelFactory
//import com.ssafy.chocopick.util.UiState
//import kotlinx.coroutines.launch
//
//class CartFragment : Fragment(R.layout.fragment_cart) {
//
//    private var _binding: FragmentCartBinding? = null
//    private val binding get() = _binding!!
//
//    private val cartViewModel: CartViewModel by activityViewModels {
//        CartViewModelFactory(requireActivity().application, FirebaseAuth.getInstance().currentUser!!.uid)
//    }
//
//    private val selectedStoreViewModel: SelectedStoreViewModel by activityViewModels {
//        SelectedStoreViewModelFactory(requireActivity().application, Gson(), FirebaseAuth.getInstance().currentUser!!.uid)
//    }
//
//    private val orderViewModel: OrderViewModel by activityViewModels {
//        OrderViewModelFactory(
//            ServiceLocator.provideOrderRepository(requireContext())
//        )
//    }
//    private val uid: String by lazy {
//        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
//    }
//
//    private var selectedOrderType: String = "PICKUP" // "PICKUP" or "STORE"
//
//    // ✅ 매장주문 대기 상태
//    private var waitingNfcForStoreOrder: Boolean = false
//    private var nfcDialog: AlertDialog? = null
//    private val cartAdapter = CartAdapter(
//        onPlus = {cartViewModel.increase(it)},
//        onMinus = {cartViewModel.decrease(it)},
//        onRemove = {cartViewModel.remove(it)}
//    )
//
//    override fun onStart() {
//        super.onStart()
//        cartViewModel.refresh()
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        _binding = FragmentCartBinding.bind(view)
//
//        binding.btnOrder.setOnClickListener {
//            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
//            if (storeId == null) {
//                Toast.makeText(requireContext(), "매장을 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            orderViewModel.placeOrder(
//                cartItems = cartViewModel.cartItems.value,
//                storeId = storeId,
//                orderType =
//            )
//        }
//
//        binding.rvCart.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = cartAdapter
//        }
//
//
//
//        binding.btnOrder.setOnClickListener {
//            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
//            if (storeId.isNullOrBlank()) {
//                Toast.makeText(requireContext(), "매장을 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val items = cartViewModel.cartItems.value
//            if (items.isEmpty()) {
//                Toast.makeText(requireContext(), "장바구니가 비어있어요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (uid.isBlank()) {
//                Toast.makeText(requireContext(), "로그인이 필요해요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // ✅ 주문은 ViewModel이 처리 (DB 저장 + 성공 후 FCM까지 여기서 하지 않음)
//            orderViewModel.placeOrder(
//                cartItems = items,
//                storeId = storeId
//            )
//        }
//
//        // 토글 버튼
//        binding.btnStoreOrder.setOnClickListener {
//            selectedOrderType = "STORE"
//            updateOrderTypeUi()
//        }
//        binding.btnPickupOrder.setOnClickListener {
//            selectedOrderType = "PICKUP"
//            updateOrderTypeUi()
//        }
//        updateOrderTypeUi()
//
//        binding.btnClear.setOnClickListener {
//            cartViewModel.clear()
//        }
//
//        collectCart()
//        collectOrderState()
//        collectNfcTagEvent()
//
//    }
//
//
////    private fun requestDelayedFcmTest(){
////        FirebaseMessaging.getInstance().token
////            .addOnSuccessListener { token ->
////                viewLifecycleOwner.lifecycleScope.launch {
////                    runCatching {
////                        ApiProvider.fcmApi.sendDelayed(
////                            FcmRequestDto(
////                                token = token,
////                                title = "주문 완료!",
////                                body = "테스트 주문입니다"
////                            )
////                        )
////                    }.onSuccess {
////                        Toast.makeText(requireContext(), "Spring 요청 성공! (0/10/20초 알림)", Toast.LENGTH_SHORT).show()
////                    }.onFailure { e ->
////                        Toast.makeText(requireContext(), "Spring 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
////                        Log.d("FCMTest","${e.message}")
////                    }
////                }
////            }
////            .addOnFailureListener { e ->
////                Toast.makeText(requireContext(), "FCM 토큰 획득 실패: ${e.message}", Toast.LENGTH_SHORT).show()
////            }
////    }
//
//
//
//    private fun collectCart() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                cartViewModel.cartItems.collect { list ->
//                    Log.d("CART_TRACE", "▶ CartFragment collect size=${list.size}")
//                    cartAdapter.submitList(list)
//                    binding.tvTotalPrice.text = "${cartViewModel.totalPrice()}원"
//                }
//            }
//        }
//    }
//
//
//    private fun collectOrderState() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                orderViewModel.orderState.collect { state ->
//                    when (state) {
//                        is UiState.Success -> {
//                            Toast.makeText(requireContext(), "주문 완료!", Toast.LENGTH_SHORT).show()
//
//                            // ✅ 주문 성공 후 장바구니 비우기
//                            cartViewModel.clear()
//
//                            // ✅ 주문 성공 후 상품목록 화면으로 이동 (원래 브랜치 흐름 반영)
//                            parentFragmentManager.beginTransaction()
//                                .replace(R.id.fragment_container, ProductListFragment())
//                                .commit()
//
//                            orderViewModel.clearState()
//                        }
//
//                        is UiState.Error -> {
//                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
//                        }
//
//                        else -> Unit
//                    }
//                }
//            }
//        }
//    }
//    private fun collectNfcTagEvent() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                nfcViewModel.tagEvent.collect {
//                    Log.d("NFC", "tagEvent received. waiting=$waitingNfcForStoreOrder")
//                    if (!waitingNfcForStoreOrder) return@collect
//
//                    // ✅ 대기 해제 + 다이얼로그 닫기
//                    waitingNfcForStoreOrder = false
//                    dismissNfcDialog()
//
//                    val storeId = selectedStoreViewModel.selectedStore.value?.storeId
//                    val items = cartViewModel.cartItems.value
//                    if (storeId.isNullOrBlank() || items.isEmpty() || uid.isBlank()) {
//                        toast("주문 정보를 확인할 수 없어요")
//                        return@collect
//                    }
//
//                    // ✅ 요구사항: 아무 NFC나 찍히면 무조건 1번 테이블
//                    orderViewModel.placeOrder(
//                        cartItems = items,
//                        storeId = storeId,
//                        orderType = "STORE",
//                        tableNo = 1
//                    )
//                }
//            }
//        }
//    }
//
//    private fun showNfcDialog() {
//        dismissNfcDialog()
//        nfcDialog = AlertDialog.Builder(requireContext())
//            .setTitle("매장 주문")
//            .setMessage("테이블의 NFC를 태깅해주세요.\n(현재 버전: 어떤 NFC든 태깅되면 1번 테이블로 처리)")
//            .setCancelable(true)
//            .setNegativeButton("취소") { d, _ ->
//                waitingNfcForStoreOrder = false
//                d.dismiss()
//            }
//            .create()
//        nfcDialog?.show()
//    }
//
//    private fun dismissNfcDialog() {
//        nfcDialog?.dismiss()
//        nfcDialog = null
//    }
//
//    private fun updateOrderTypeUi() {
//        // ✅ 최소 동작: 선택 상태만 유지
//        // 필요하면 여기서 버튼 색/스트로크/alpha 등을 바꾸면 됨
//        val isStore = selectedOrderType == "STORE"
//        binding.btnStoreOrder.isSelected = isStore
//        binding.btnPickupOrder.isSelected = !isStore
//    }
//
//    private fun toast(msg: String) {
//        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        dismissNfcDialog()
//        _binding = null
//    }
//}
package com.ssafy.chocopick.ui.order

import android.content.res.ColorStateList
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentCartBinding
import com.ssafy.chocopick.ui.common.NfcViewModel
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModel
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModelFactory
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val uid: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    private val cartViewModel: CartViewModel by activityViewModels {
        CartViewModelFactory(requireActivity().application, uid)
    }

    private val selectedStoreViewModel: SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(requireActivity().application, Gson(), uid)
    }

    private val orderViewModel: OrderViewModel by activityViewModels {
        OrderViewModelFactory(ServiceLocator.provideOrderRepository(requireContext()))
    }

    // ✅ 추가: CartFragment에서도 같은 Activity 범위 ViewModel로 받아야 함
    private val nfcViewModel: NfcViewModel by activityViewModels()

    private val cartAdapter = CartAdapter(
        onPlus = { cartViewModel.increase(it) },
        onMinus = { cartViewModel.decrease(it) },
        onRemove = { cartViewModel.remove(it) }
    )

    private var selectedOrderType: String = "PICKUP" // "PICKUP" or "STORE"
    private var waitingNfcForStoreOrder: Boolean = false
    private var nfcDialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        cartViewModel.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCartBinding.bind(view)

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        // ✅ 토글 버튼
        binding.btnStoreOrder.setOnClickListener {
            selectedOrderType = "STORE"
            updateOrderTypeUi()
        }
        binding.btnPickupOrder.setOnClickListener {
            selectedOrderType = "PICKUP"
            updateOrderTypeUi()
        }
        updateOrderTypeUi()

        binding.btnClear.setOnClickListener { cartViewModel.clear() }

        // ✅ btnOrder 리스너는 "딱 1개"만 존재해야 함
        binding.btnOrder.setOnClickListener {
            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
            if (storeId.isNullOrBlank()) { toast("매장을 먼저 선택해주세요"); return@setOnClickListener }

            val items = cartViewModel.cartItems.value
            if (items.isEmpty()) { toast("장바구니가 비어있어요"); return@setOnClickListener }

            if (uid.isBlank()) { toast("로그인이 필요해요"); return@setOnClickListener }

            if (selectedOrderType == "PICKUP") {
                // ✅ 픽업 주문은 즉시 주문
                orderViewModel.placeOrder(
                    cartItems = items,
                    storeId = storeId,
                    orderType = "PICKUP",
                    tableNo = null
                )
            } else {
                // ✅ 매장 주문은 NFC 태깅 대기
                waitingNfcForStoreOrder = true
                showNfcDialog()
            }
        }

        collectCart()
        collectOrderState()
        collectNfcTagEvent()
    }

    private fun collectCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collect { list ->
                    cartAdapter.submitList(list)
                    binding.tvTotalPrice.text = "${cartViewModel.totalPrice()}원"
                }
            }
        }
    }

    private fun collectOrderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                orderViewModel.orderState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            toast("주문 완료!")
                            cartViewModel.clear()

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, ProductListFragment())
                                .commit()

                            orderViewModel.clearState()
                        }
                        is UiState.Error -> toast(state.message)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun collectNfcTagEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                nfcViewModel.tagEvent.collect {
                    Log.d("NFC", "tagEvent received. waiting=$waitingNfcForStoreOrder")
                    if (!waitingNfcForStoreOrder) return@collect

                    waitingNfcForStoreOrder = false
                    dismissNfcDialog()

                    val storeId = selectedStoreViewModel.selectedStore.value?.storeId
                    val items = cartViewModel.cartItems.value
                    if (storeId.isNullOrBlank() || items.isEmpty() || uid.isBlank()) {
                        toast("주문 정보를 확인할 수 없어요")
                        return@collect
                    }

                    // ✅ 어떤 NFC든 1번 테이블
                    orderViewModel.placeOrder(
                        cartItems = items,
                        storeId = storeId,
                        orderType = "STORE",
                        tableNo = 1
                    )
                }
            }
        }
    }

    private fun showNfcDialog() {
        dismissNfcDialog()
        nfcDialog = AlertDialog.Builder(requireContext())
            .setTitle("매장 주문")
            .setMessage("테이블의 NFC를 태깅해주세요.\n(현재 버전: 어떤 NFC든 태깅되면 1번 테이블로 처리)")
            .setCancelable(true)
            .setNegativeButton("취소") { d, _ ->
                waitingNfcForStoreOrder = false
                d.dismiss()
            }
            .create()
        nfcDialog?.show()
    }

    private fun dismissNfcDialog() {
        nfcDialog?.dismiss()
        nfcDialog = null
    }

    private fun updateOrderTypeUi() {
        val isStore = selectedOrderType == "STORE"

        fun applySelectedStyle(btn: com.google.android.material.button.MaterialButton) {
            btn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.choco_primary)
            )
            btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            btn.strokeWidth = 0
            btn.alpha = 1f
        }

        fun applyUnselectedStyle(btn: com.google.android.material.button.MaterialButton) {
            btn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.bg_surface)
            )
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_1dp) // 아래 dimen 추가
            btn.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.divider)
            )
            btn.alpha = 1f
        }

        if (isStore) {
            applySelectedStyle(binding.btnStoreOrder)
            applyUnselectedStyle(binding.btnPickupOrder)
        } else {
            applyUnselectedStyle(binding.btnStoreOrder)
            applySelectedStyle(binding.btnPickupOrder)
        }
    }
    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissNfcDialog()
        _binding = null
    }
}
