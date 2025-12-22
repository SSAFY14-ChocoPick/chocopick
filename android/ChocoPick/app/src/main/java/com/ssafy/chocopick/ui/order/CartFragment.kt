package com.ssafy.chocopick.ui.order

import android.os.Bundle
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
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.data.model.FcmRequestDto
import com.ssafy.chocopick.data.remote.ApiProvider
import com.ssafy.chocopick.databinding.FragmentCartBinding
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // ✅ ProductDetailFragment와 "같은" CartViewModel을 보려면 activityViewModels
    private val cartViewModel: CartViewModel by activityViewModels{
        CartViewModelFactory(requireActivity().application, FirebaseAuth.getInstance().currentUser!!.uid)
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
            requestDelayedFcmTest()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}