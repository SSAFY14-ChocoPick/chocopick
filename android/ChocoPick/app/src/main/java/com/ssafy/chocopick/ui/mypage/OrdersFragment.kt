package com.ssafy.chocopick.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val adapter = OrdersAdapter { order ->
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderDetailFragment.newInstance(order.orderId))
            .addToBackStack("ORDER_DETAIL")
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        // ✅ 지금은 UI 확인용 더미 (나중에 ViewModel로 교체)
        adapter.submitList(
            listOf(
                OrderListUi("o_001", "초코픽 강남점", "READY", "2025.12.19 09:12", 8900),
                OrderListUi("o_002", "초코픽 역삼점", "PICKED_UP", "2025.12.16 18:03", 6500),
                OrderListUi("o_003", "초코픽 동탄점", "PAID", "2025.12.10 12:20", 9200),
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
