package com.ssafy.chocopick.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.databinding.FragmentFavoriteStoresBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class FavoriteStoresFragment : Fragment() {

    private var _binding: FragmentFavoriteStoresBinding? = null
    private val binding get() = _binding!!

    private val vm: FavoriteStoresViewModel by viewModels {
        FavoriteStoresViewModelFactory()
    }


    private val adapter = FavoriteStoresAdapter { storeWithId ->
        Toast.makeText(
            requireContext(),
            "${storeWithId.store.name} 클릭(추후 구현)",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteStoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { goBack() }

        binding.rvFavoriteStores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteStores.adapter = adapter

        collectStores()
        vm.loadStores()
    }

    private fun collectStores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.storesState.collect { state ->
                    when (state) {
                        is UiState.Success -> adapter.submitList(state.data)
                        is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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

// 선택: 화면 표시용 유틸
private fun com.ssafy.chocopick.data.source.firebase.realtime.StoreWithId.storeIdText(): String {
    return "${store.name} (${storeId})"
}
