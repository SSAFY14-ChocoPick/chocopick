package com.ssafy.chocopick.ui.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.data.model.OrderDetailUi
import com.ssafy.chocopick.databinding.FragmentOrderDetailBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ARG_ORDER_ID = "orderId"

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderDetailViewModel by viewModels {
        OrderDetailViewModelFactory()
    }

    private val orderId: String by lazy {
        arguments?.getString(ARG_ORDER_ID).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        collectOrder()
        viewModel.loadOrder(orderId)
    }

    private fun collectOrder() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderState.collect { state ->
                    when (state) {

                        is UiState.Loading -> {
                            // 🔥 로딩 중: 스피너만
                            binding.progress.visibility = View.VISIBLE
                            binding.layoutStore.visibility = View.GONE
                            binding.tvStoreAddress.visibility = View.GONE
                            binding.tvStatus.visibility = View.GONE
                            binding.tvOrderId.visibility = View.GONE
                            binding.tvDate.visibility = View.GONE
                            binding.dividerMenu.visibility = View.GONE
                            binding.tvMenuTitle.visibility = View.GONE
                            binding.tvItems.visibility = View.GONE
                            binding.tvTotal.visibility = View.GONE
                        }

                        is UiState.Success -> {
                            // 🔥 로딩 완료: 컨텐츠만
                            binding.progress.visibility = View.GONE
                            binding.layoutStore.visibility = View.VISIBLE
                            binding.tvStoreAddress.visibility = View.VISIBLE
                            binding.tvStatus.visibility = View.VISIBLE
                            binding.tvOrderId.visibility = View.VISIBLE
                            binding.tvDate.visibility = View.VISIBLE
                            binding.dividerMenu.visibility = View.VISIBLE
                            binding.tvMenuTitle.visibility = View.VISIBLE
                            binding.tvItems.visibility = View.VISIBLE
                            binding.tvTotal.visibility = View.VISIBLE
                            bind(state.data)
                        }

                        is UiState.Error -> {
                            // 🔥 실패: 스피너 제거 + 빈 화면
                            binding.progress.visibility = View.GONE
                            binding.layoutStore.visibility = View.VISIBLE
                            binding.tvStoreAddress.visibility = View.VISIBLE
                            binding.tvStatus.visibility = View.VISIBLE
                            binding.tvOrderId.visibility = View.VISIBLE
                            binding.tvDate.visibility = View.VISIBLE
                            binding.dividerMenu.visibility = View.VISIBLE
                            binding.tvMenuTitle.visibility = View.VISIBLE
                            binding.tvItems.visibility = View.VISIBLE
                            binding.tvTotal.visibility = View.VISIBLE
                            // (토스트 정도는 optional)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun bind(data: OrderDetailUi) = with(binding) {
        val order = data.orderWithStore.order
        val store = data.store

        /* =========================
         * 매장 정보
         * ========================= */

        // 매장명
        tvStore.text =
            data.orderWithStore.storeName.ifBlank { data.orderWithStore.storeId }

        // 주소
        tvStoreAddress.text =
            store?.address ?: "주소 정보 없음"

        // 지도 버튼 (매장 정보 없으면 숨김)
        btnMap.visibility = if (store != null) View.VISIBLE else View.GONE
        btnMap.setOnClickListener {
            store?.let {
                openMap(it.lat, it.lng, it.name)
            }
        }

        /* =========================
         * 주문 기본 정보
         * ========================= */

        tvStatus.text = order.status
        tvOrderId.text = "주문번호 #${order.orderId}"
        tvDate.text = formatDate(order.orderDate)

        /* =========================
         * 주문 메뉴 (배민 스타일)
         * ========================= */

        tvItems.text = order.items.joinToString("\n") {
            "• ${it.name}  × ${it.quantity}"
        }

        /* =========================
         * 결제 금액
         * ========================= */

        tvTotal.text =
            "₩ ${DecimalFormat("#,###").format(order.totalPrice)}"
    }

    private fun openMap(lat: Double, lng: Double, name: String) {
        val uri = Uri.parse(
            "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(name)})"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(intent)
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
            .format(Date(millis))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(orderId: String) =
            OrderDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_ID, orderId)
                }
            }
    }
}
