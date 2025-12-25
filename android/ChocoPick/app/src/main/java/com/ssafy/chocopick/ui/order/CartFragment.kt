
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
                nfcViewModel.startWaiting()
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

                // ✅ 이거 추가: ReaderMode 끄기
                nfcViewModel.stopWaiting()

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
