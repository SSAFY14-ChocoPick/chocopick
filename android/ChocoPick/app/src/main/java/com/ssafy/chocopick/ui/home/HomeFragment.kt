package com.ssafy.chocopick.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentHomeBinding
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModel
import com.ssafy.chocopick.ui.home.store.SelectedStoreViewModelFactory
import com.ssafy.chocopick.ui.home.store.StoreListFragment
import com.ssafy.chocopick.ui.home.store.StoreMapFragment
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private val selectedStoreVM : SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(app = requireActivity().application, uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)
        binding.storeChoiceBtn.setOnClickListener {
            showStoreChoiceDialog()
        }

        setUpStoreText()
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
                        binding.tvSelectedStore.text = "📍 ${store.name} 선택됨"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}