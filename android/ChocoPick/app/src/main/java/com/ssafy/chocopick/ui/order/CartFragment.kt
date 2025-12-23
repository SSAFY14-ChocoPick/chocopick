package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.ssafy.chocopick.R
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.FcmRequestDto
import com.ssafy.chocopick.data.remote.ApiProvider
import com.ssafy.chocopick.data.source.firebase.realtime.RealtimeDbClient
import com.ssafy.chocopick.databinding.FragmentCartBinding
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModel
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModelFactory
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels {
        CartViewModelFactory(requireActivity().application, FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val selectedStoreViewModel: SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(requireActivity().application, Gson(), FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val orderViewModel: OrderViewModel by activityViewModels {
        OrderViewModelFactory(
            ServiceLocator.provideOrderRepository(requireContext())
        )
    }

    private val cartAdapter = CartAdapter(
        onPlus = {cartViewModel.increase(it)},
        onMinus = {cartViewModel.decrease(it)},
        onRemove = {cartViewModel.remove(it)}
    )

    override fun onStart() {
        super.onStart()
        cartViewModel.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCartBinding.bind(view)

        binding.btnOrder.setOnClickListener {
            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
            if (storeId == null) {
                Toast.makeText(requireContext(), "매장을 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            orderViewModel.placeOrder(
                cartItems = cartViewModel.cartItems.value,
                storeId = storeId
            )
        }

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        collectOrderState()
        collectCart()
    }

    private fun collectCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collect { list ->
                    Log.d("CART_TRACE", "▶ CartFragment collect size=${list.size}")
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
                            Toast.makeText(requireContext(), "주문 완료!", Toast.LENGTH_SHORT).show()
                            cartViewModel.clear()
                            orderViewModel.clearState()
                        }
                        is UiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
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
