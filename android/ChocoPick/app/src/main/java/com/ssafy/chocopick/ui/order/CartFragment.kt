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
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // ✅ ProductDetailFragment와 "같은" CartViewModel을 보려면 activityViewModels
    private val cartViewModel: CartViewModel by activityViewModels{
        CartViewModelFactory(requireActivity().application, FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val SelectedstoreViewModel : SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(requireActivity().application, Gson(), FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val cartAdapter = CartAdapter(
        onPlus = { productId -> cartViewModel.increase(productId) },
        onMinus = { productId -> cartViewModel.decrease(productId) },
        onRemove = { productId -> cartViewModel.remove(productId) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCartBinding.bind(view)

        // bottomBar 높이만큼 scrollView paddingBottom 동적 적용
        binding.bottomBar.post {
            val barH = binding.bottomBar.height
            binding.scrollView.setPadding(
                binding.scrollView.paddingLeft,
                binding.scrollView.paddingTop,
                binding.scrollView.paddingRight,
                barH + resources.getDimensionPixelSize(R.dimen.cart_scroll_extra_bottom)
            )
        }

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        binding.btnOrder.setOnClickListener {
            // 비동기 처리로 버튼 클릭 시 UI 스레드가 차단되지 않도록 합니다
            lifecycleScope.launch {
                saveOrderToFirebase(cartViewModel.cartItems.value)
            }
        }

        binding.btnClear.setOnClickListener {
            cartViewModel.clear()
        }

        collectCart()


        // ✅ 화면 들어왔을 때 최신 값 반영
        cartViewModel.refresh()
    }

    private fun requestDelayedFcmTest(){
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        ApiProvider.fcmApi.sendDelayed(
                            FcmRequestDto(
                                token = token,
                                title = "주문 완료!",
                                body = "테스트 주문입니다"
                            )
                        )
                    }.onSuccess {
                        Toast.makeText(requireContext(), "Spring 요청 성공! (0/10/20초 알림)", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Spring 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.d("FCMTest","${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "FCM 토큰 획득 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun collectCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collect { list ->
                    cartAdapter.submitList(list)
                    binding.tvTotalPrice.text = "총액: ${cartViewModel.totalPrice()}원"
                }
            }
        }
    }

    private suspend fun saveOrderToFirebase(cartItems: List<CartItem>) {
        val dbClient = RealtimeDbClient()  // RealtimeDbClient 인스턴스 생성
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val orderId = dbClient.pushKey("all_orders")  // Firebase에서 push()로 새로운 주문 ID 생성

        // 주문 데이터 구성
        val orderData = hashMapOf(
            "userId" to userId,
            "items" to cartItems.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "name" to item.name,
                    "price" to item.price,
                    "quantity" to item.quantity
                )
            },
            "totalPrice" to cartViewModel.totalPrice(),
            "orderDate" to System.currentTimeMillis(),
            "store" to SelectedstoreViewModel.selectedStore.value?.storeId,
            "status" to "주문 완료"
        )

        val userOrderData: HashMap<String, Any> = hashMapOf(
            orderId to orderData
        )

        try {
            // 1. 사용자별 주문 데이터 저장 (orders_eachUser)
            dbClient.set("orders_eachUser/$userId/$orderId", orderData)

            // 2. 전체 주문 데이터 저장 (all_orders)
            dbClient.set("all_orders/$orderId", orderData)

            // 주문 저장 후, FCM 발송 함수 호출
            sendFcmNotification()

            Toast.makeText(requireContext(), "주문 저장 완료", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            // 오류 처리
            Toast.makeText(requireContext(), "주문 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("FirebaseOrderSave", "주문 저장 실패", e)
        }
    }
    private fun sendFcmNotification() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        ApiProvider.fcmApi.sendDelayed(
                            FcmRequestDto(
                                token = token,
                                title = "주문 완료!",
                                body = "테스트 주문입니다"
                            )
                        )
                    }.onSuccess {
                        Toast.makeText(requireContext(), "Spring 요청 성공! (0/10/20초 알림)", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Spring 요청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.d("FCMTest","${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "FCM 토큰 획득 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}