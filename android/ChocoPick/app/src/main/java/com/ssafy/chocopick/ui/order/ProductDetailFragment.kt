package com.ssafy.chocopick.ui.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.databinding.FragmentProductDetailBinding
import com.ssafy.chocopick.ui.review.ReviewsFragment
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

    private val cartViewModel : CartViewModel by activityViewModels{
        CartViewModelFactory(requireActivity().application, FirebaseAuth.getInstance().currentUser!!.uid)
    }

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

    private var qty: Int = 1
    private var currentProduct: Product? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)
        binding.btnGoReviews.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReviewsFragment.newInstance(productId, /*myNickname*/ ""))
                .addToBackStack("REVIEWS")
                .commit()
        }
        setupQtyUi()
        setUpAddToCartClick()

        collectProduct() //상품 정보 로드
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

    private fun setUpAddToCartClick() {
        binding.btnAddToCart.setOnClickListener {
            val p = currentProduct ?: return@setOnClickListener
            cartViewModel.addToCart(p, qty)

            // ✅ 여기서 "장바구니 담기" 실제 로직 연결하면 됨
            // 지금은 UI 플로우 먼저라 했으니, 일단 성공했다고 가정하고 다이얼로그 띄움
            // ex) viewModel.addToCart(p.productId, qty) 같은 걸 나중에 붙이면 됨

            showGoToCartDialog()
        }
    }

    private fun setupQtyUi(){
        binding.tvQty.text = qty.toString()

        binding.btnMinus.setOnClickListener {
            if (qty > 1) {
                qty--
                binding.tvQty.text = qty.toString()
            }
        }

        binding.btnPlus.setOnClickListener {
            qty++
            binding.tvQty.text = qty.toString()
        }
    }


    private fun showGoToCartDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("장바구니에 담았어요.")
            .setMessage("장바구니 화면으로 이동할까요?")
            .setPositiveButton("네"){
                _,_ ->
                parentFragmentManager.popBackStack()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container,CartFragment())
                    .addToBackStack(null).commit()
            }
            .setNegativeButton("아니요"){
                _,_ ->
                parentFragmentManager.popBackStack()
            }
            .show()
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
                            currentProduct = p

                            binding.productNameTv.text = "제품명 : ${p.name}"
                            binding.productIdTv.text = "상품 ID : ${p.productId}"
                            binding.productPriceTv.text = "가격 : ${p.price}원"
                            binding.productWeightTv.text = "총 내용량 : ${p.weight}"
                            binding.productTypeTv.text = "식품의 유형 : ${p.type}"
                            binding.ProductOriginTv.text = "원산지 : ${p.origin}"
                            binding.ProductManufactureTv.text = "제조(수입)업체 : ${p.manufacturer}"

                            if(p.imageUrl.isNotBlank()){
                                Glide.with(binding.ivProduct)
                                    .load(p.imageUrl)
                                    .into(binding.ivProduct)
                            }else{
                                binding.ivProduct.setImageDrawable(null)
                            }
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