package com.ssafy.chocopick.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentOrdersBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val myPageViewModel: MyPageViewModel by viewModels {
        MyPageViewModelFactory()
    }

    private val adapter = OrdersAdapter { order ->
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderDetailFragment.newInstance(order.orderId))
            .addToBackStack("ORDER_DETAIL")
            .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        collectOrders()
        myPageViewModel.loadOrderList()
    }

    private fun collectOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.ordersState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // 로딩 UI 있으면 표시
                        }
                        is UiState.Success -> {
                            adapter.submitList(state.data)
                        }
                        is UiState.Error -> {
                            // 빈 리스트 처리하거나 토스트
                            adapter.submitList(emptyList())
                        }
                        else -> Unit
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

