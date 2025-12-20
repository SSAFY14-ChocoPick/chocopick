package com.ssafy.chocopick.ui.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.ssafy.chocopick.R
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
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

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        collectCart()

        // ✅ 화면 들어왔을 때 최신 값 반영
        cartViewModel.refresh()
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