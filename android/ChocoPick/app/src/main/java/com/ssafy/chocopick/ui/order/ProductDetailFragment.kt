package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentProductDetailBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PRODUCT_ID = "productId"

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private var _binding : FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        @JvmStatic
        fun newInstance(productId : String) =
            ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUCT_ID,productId)
                }
            }
    }
    private val productId : String by lazy{
        arguments?.getString(ARG_PRODUCT_ID).orEmpty()
    }

    private val viewModel : ProductDetailViewModel by viewModels {
        ProductDetailViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)
        collectProduct()
        viewModel.loadProductDetail(productId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProductDetailFragment","${productId}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    private fun collectProduct(){
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.productDetailState.collect {
                    state ->
                    when(state){
                        is UiState.Idle -> Unit
                        is UiState.Loading -> {

                        }
                        is UiState.Success -> {
                            val p = state.data
                            binding.productIdTv.text = p.productId
                            binding.productNameTv.text = p.name
                            binding.productPriceTv.text = "${p.price}원"
                            binding.productWeightTv.text = p.weight
                            binding.productTypeTv.text = p.type
                            binding.ProductOriginTv.text = p.origin
                            binding.ProductManufactureTv.text = p.manufacturer
                        }
                        is UiState.Error -> {
                            Toast.makeText(requireContext(),state.message,Toast.LENGTH_SHORT).show()
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