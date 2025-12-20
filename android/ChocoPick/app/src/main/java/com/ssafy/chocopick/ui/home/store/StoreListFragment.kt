package com.ssafy.chocopick.ui.home.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentStoreListBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StoreListFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentStoreListBinding? = null
    private val binding get() = _binding!!

    private val vm : StoreListViewModel by viewModels {
        StoreListViewModelFactory()
    }

    private val selectedStoreVm : SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(app = requireActivity().application, uid = FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private val adapter = StoreListAdapter { store ->
        selectedStoreVm.select(store)
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStoreListBinding.bind(view)
        binding.rvStores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStores.adapter = adapter

        collectStores()
        vm.loadStores()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store_list, container, false)
    }

    fun collectStores(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.storesState.collect {
                    state ->
                    when(state){
                        is UiState.Idle -> {
                            binding.progress.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                        }
                        is UiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.progress.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.progress.visibility = View.GONE
                            val list = state.data
                            adapter.submitList(list)
                            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StoreListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}