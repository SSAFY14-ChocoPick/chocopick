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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.data.repository.StoreRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.databinding.FragmentStoreMapBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StoreMapFragment : Fragment(R.layout.fragment_store_map), GoogleMap.OnMarkerClickListener{

    private var _binding : FragmentStoreMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap : GoogleMap? = null
    private val markerToStore = mutableMapOf<Marker, Store>()
    private var selectedOnMap : Store? = null

    private val storeRepo : StoreRepository by lazy {
        StoreRepositoryImpl(ds = StoreDataSource())
    }

    private val mapVm : StoreMapViewModel by viewModels {
        StoreMapViewModelFactory(storeRepo)
    }

    private val uid: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
    private val selectedStoreVM: SelectedStoreViewModel by activityViewModels {
        SelectedStoreViewModelFactory(app = requireActivity().application, uid = uid)
    }

    private var param1: String? = null
    private var param2: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStoreMapBinding.bind(view)

        setupMap()
        setupSelectButton()
        collectStores()

        mapVm.loadStore()

    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            map.setOnMarkerClickListener(this)

            map.uiSettings.isZoomControlsEnabled = true //+- 버튼
            map.uiSettings.isZoomGesturesEnabled = true //줌
            map.uiSettings.isScrollGesturesEnabled = true //스크롤
            map.uiSettings.isRotateGesturesEnabled = false //회전
            map.uiSettings.isTiltGesturesEnabled = false //기울기

            // 기본 위치(예: 서울) - 너희 매장 지역에 맞춰 바꿔도 됨
            val seoul = LatLng(37.5665, 126.9780)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))
        }
    }

    private fun collectStores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapVm.storesState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> {
                            // 필요하면 로딩 UI 추가
                        }
                        is UiState.Success -> {
                            renderMarkers(state.data)
                        }
                        is UiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun renderMarkers(stores: List<Store>) {
        val map = googleMap ?: return

        map.clear()
        markerToStore.clear()
        selectedOnMap = null
        binding.storeSheet.visibility = View.GONE

        if (stores.isEmpty()) return

        // 모든 매장 마커 추가
        stores.forEach { store ->
            val pos = LatLng(store.lat, store.lng)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(store.name)
                    .snippet(store.address)
            )
            if (marker != null) markerToStore[marker] = store
        }

        // 첫 매장 기준으로 카메라 이동
        val first = stores.first()
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.lat, first.lng), 13f))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val store = markerToStore[marker] ?: return false
        selectedOnMap = store

        binding.tvStoreName.text = store.name
        binding.tvStoreAddr.text = store.address
        binding.storeSheet.visibility = View.VISIBLE

        // 마커 기본 동작(InfoWindow)도 같이 띄우고 싶으면:
        marker.showInfoWindow()

        return true // 우리가 이벤트 소비
    }

    private fun setupSelectButton() {
        binding.btnSelectStore.setOnClickListener {
            val store = selectedOnMap
            if (store == null) {
                Toast.makeText(requireContext(), "먼저 매장을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedStoreVM.select(store)
            Toast.makeText(requireContext(), "매장 선택 완료: ${store.name}", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        googleMap = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StoreMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}