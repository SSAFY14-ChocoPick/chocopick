package com.ssafy.chocopick.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ssafy.chocopick.databinding.FragmentOrderDetailBinding
import java.text.DecimalFormat

private const val ARG_ORDER_ID = "orderId"

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

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

        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // ✅ 지금은 UI 확인용 더미 (나중에 RTDB에서 orderId로 로드)
        binding.tvOrderId.text = "주문번호: #$orderId"
        binding.tvStore.text = "초코픽 강남점"
        binding.tvStatus.text = "READY"
        binding.tvDate.text = "2025.12.19 09:12"
        binding.tvItems.text = "다크초코 라떼 x1\n브라우니 x1"
        binding.tvTotal.text = "총 결제금액: ₩ ${DecimalFormat("#,###").format(8900)}"
    }

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
