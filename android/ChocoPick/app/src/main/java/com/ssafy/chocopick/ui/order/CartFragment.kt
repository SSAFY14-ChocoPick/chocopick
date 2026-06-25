package com.ssafy.chocopick.ui.order

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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

    // ✅ 변경: OrderViewModelFactory에 RewardRepository도 주입
    private val orderViewModel: OrderViewModel by activityViewModels {
        OrderViewModelFactory(
            orderRepository = ServiceLocator.provideOrderRepository(requireContext()),
            rewardRepository = ServiceLocator.provideRewardRepository()
        )
    }


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

        binding.btnOrder.setOnClickListener {
            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
            if (storeId.isNullOrBlank()) { toast(getString(R.string.cart_select_store_first)); return@setOnClickListener }

            val items = cartViewModel.cartItems.value
            if (items.isEmpty()) { toast(getString(R.string.cart_empty)); return@setOnClickListener }

            if (uid.isBlank()) { toast(getString(R.string.login_required)); return@setOnClickListener }

            if (selectedOrderType == "PICKUP") {
                orderViewModel.placeOrder(
                    cartItems = items,
                    storeId = storeId,
                    orderType = "PICKUP",
                    tableNo = null
                )
            } else {
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
                            toast(getString(R.string.order_complete))
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
                        toast(getString(R.string.order_info_invalid))
                        return@collect
                    }

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
            .setTitle(R.string.cart_nfc_dialog_title)
            .setMessage(R.string.cart_nfc_dialog_message)
            .setCancelable(true)
            .setNegativeButton(R.string.common_cancel) { d, _ ->
                waitingNfcForStoreOrder = false
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
            btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_1dp)
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
