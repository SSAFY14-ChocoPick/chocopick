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
import com.ssafy.chocopick.databinding.FragmentSelectFavoriteStoresBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class SelectFavoriteStoresFragment : Fragment() {

    private var _binding: FragmentSelectFavoriteStoresBinding? = null
    private val binding get() = _binding!!

    private val vm: FavoriteStoresViewModel by viewModels { FavoriteStoresViewModelFactory() }

    private val adapter = AllStoresAdapter(emptySet()) { storeId, newValue ->
        vm.toggleFavorite(storeId, newValue)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSelectFavoriteStoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.rvAllStores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllStores.adapter = adapter

        collect()
        vm.loadAllStoresAndFavorites()
    }

    private fun collect() {
        // 전체 매장 로드
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.allStoresState.collect { state ->
                    if (state is UiState.Success) {
                        adapter.submitList(state.data, vm.favoriteIdsSnapshot())
                    }
                }
            }
        }
        // 즐겨찾기 id 변경 시(별 토글) 즉시 반영
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.favoriteIdsState.collect { state ->
                    if (state is UiState.Success) {
                        val stores = (vm.allStoresState.value as? UiState.Success)?.data.orEmpty()
                        adapter.submitList(stores, state.data)
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
