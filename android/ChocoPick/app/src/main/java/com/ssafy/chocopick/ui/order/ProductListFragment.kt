package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentProductListBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class ProductListFragment : Fragment(R.layout.fragment_product_list) {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductListViewModel by viewModels {
        ProductListViewModelFactory()
    }

    private val productAdapter = ProductAdapter(
        onItemClick = {
            clicked ->
            val detail = ProductDetailFragment.newInstance(
                clicked.productId
            )

            parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                detail).addToBackStack(null).commit()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductListBinding.bind(view)

        setupRecyclerView()
        setupFAB()
        collectProducts()

        // 🔥 Fragment 진입 시 상품 로드
        viewModel.loadProducts()
    }

    private fun setupFAB() {
        binding.fabCart.setOnClickListener {
            parentFragmentManager.beginTransaction().
            replace(R.id.fragment_container, CartFragment())
                .addToBackStack(null).commit()
        }
    }

    private fun setupRecyclerView() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun collectProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsState.collect { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.progress.visibility = View.GONE
                        }

                        is UiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                        }

                        is UiState.Success -> {
                            binding.progress.visibility = View.GONE
                            productAdapter.submitList(state.data)
                        }

                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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