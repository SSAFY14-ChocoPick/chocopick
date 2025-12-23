package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.ssafy.chocopick.R
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.replace
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
    private val uid: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
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

        binding.btnOrder.setOnClickListener {
            val storeId = selectedStoreViewModel.selectedStore.value?.storeId
            if (storeId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "매장을 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val items = cartViewModel.cartItems.value
            if (items.isEmpty()) {
                Toast.makeText(requireContext(), "장바구니가 비어있어요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uid.isBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요해요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 주문은 ViewModel이 처리 (DB 저장 + 성공 후 FCM까지 여기서 하지 않음)
            orderViewModel.placeOrder(
                cartItems = items,
                storeId = storeId
            )
        }

        binding.btnClear.setOnClickListener {
            cartViewModel.clear()
        }

        collectCart()
        collectOrderState()


    }


//    private fun requestDelayedFcmTest(){
//        FirebaseMessaging.getInstance().token
//            .addOnSuccessListener { token ->
//                viewLifecycleOwner.lifecycleScope.launch {
//                    runCatching {
//                        ApiProvider.fcmApi.sendDelayed(
//                            FcmRequestDto(
//                                token = token,
//                                title = "주문 완료!",
//                                body = "테스트 주문입니다"
//                            )
//                        )
//                    }.onSuccess {
//                        Toast.makeText(requireContext(), "Spring 요청 성공! (0/10/20초 알림)", Toast.LENGTH_SHORT).show()
//                    }.onFailure { e ->
//                        Toast.makeText(requireContext(), "Spring 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//                        Log.d("FCMTest","${e.message}")
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "FCM 토큰 획득 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }



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

                            // ✅ 주문 성공 후 장바구니 비우기
                            cartViewModel.clear()

                            // ✅ 주문 성공 후 상품목록 화면으로 이동 (원래 브랜치 흐름 반영)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, ProductListFragment())
                                .commit()

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
