package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentProductListBinding
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModel
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModelFactory
import com.ssafy.chocopick.ui.home.store.StoreListFragment
import com.ssafy.chocopick.ui.home.store.StoreMapFragment
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch
import kotlin.getValue

class ProductListFragment : Fragment(R.layout.fragment_product_list) {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductListViewModel by viewModels {
        ProductListViewModelFactory()
    }

    private val selectedStoreVM : SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(app = requireActivity().application, uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
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

        binding.storeChoiceBtn.setOnClickListener {
            showStoreChoiceDialog()
        }

        setUpStoreText()

        setupRecyclerView()
        setupFAB()
        collectProducts()

        // 🔥 Fragment 진입 시 상품 로드
        viewModel.loadProducts()
    }


    fun showStoreChoiceDialog(){
        val items = arrayOf("지도에서 선택", "목록에서 선택")
        AlertDialog.Builder(requireContext())
            .setTitle("매장 선택 방식")
            .setItems(items){_,which ->
                when(which) {
                    0 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, StoreMapFragment()).addToBackStack(null)
                            .commit()
                    }
                    1 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, StoreListFragment()).addToBackStack(null)
                            .commit()
                    }
                }
            }
            .show()
    }

    fun setUpStoreText(){

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                selectedStoreVM.selectedStore.collect { store ->
                    if (store == null) {
                        binding.tvSelectedStore.text = "아직 선택된 매장이 없어요"
                    } else {
                        binding.tvSelectedStore.text = "${store.name}"
                    }
                }
            }
        }
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