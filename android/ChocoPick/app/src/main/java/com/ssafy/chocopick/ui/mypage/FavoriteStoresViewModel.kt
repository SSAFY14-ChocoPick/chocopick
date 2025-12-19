package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.FavoriteRepository
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteStoresViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _allStoresState = MutableStateFlow<UiState<List<Store>>>(UiState.Idle)
    val allStoresState: StateFlow<UiState<List<Store>>> = _allStoresState

    private val _favoriteIdsState = MutableStateFlow<UiState<Set<String>>>(UiState.Idle)
    val favoriteIdsState: StateFlow<UiState<Set<String>>> = _favoriteIdsState

    private val _favoriteStoresState = MutableStateFlow<UiState<List<Store>>>(UiState.Idle)
    val favoriteStoresState: StateFlow<UiState<List<Store>>> = _favoriteStoresState

    fun favoriteIdsSnapshot(): Set<String> =
        (favoriteIdsState.value as? UiState.Success)?.data ?: emptySet()

    fun loadAllStoresAndFavorites() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _allStoresState.value = UiState.Error("로그인이 필요합니다.")
            _favoriteIdsState.value = UiState.Error("로그인이 필요합니다.")
            _favoriteStoresState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        _allStoresState.value = UiState.Loading
        _favoriteIdsState.value = UiState.Loading
        _favoriteStoresState.value = UiState.Loading

        viewModelScope.launch {
            runCatching { storeRepository.getAllStores() }
                .onSuccess { stores ->
                    _allStoresState.value = UiState.Success(stores)
                    // 즐겨찾기 id가 이미 로드되어 있으면 즐겨찾기 매장 목록도 즉시 계산
                    val favIds = favoriteIdsSnapshot()
                    if (favIds.isNotEmpty()) {
                        _favoriteStoresState.value = UiState.Success(stores.filter { favIds.contains(it.storeId) })
                    }
                }
                .onFailure { e ->
                    _allStoresState.value = UiState.Error(e.message ?: "매장 목록 로드 실패", e)
                }
        }

        viewModelScope.launch {
            runCatching { favoriteRepository.getFavoriteStoreIds(uid) }
                .onSuccess { favIds ->
                    _favoriteIdsState.value = UiState.Success(favIds)

                    val stores = (allStoresState.value as? UiState.Success)?.data.orEmpty()
                    _favoriteStoresState.value = UiState.Success(stores.filter { favIds.contains(it.storeId) })
                }
                .onFailure { e ->
                    _favoriteIdsState.value = UiState.Error(e.message ?: "즐겨찾기 로드 실패", e)
                }
        }
    }

    fun toggleFavorite(storeId: String, newValue: Boolean) {
        val uid = authRepository.getCurrentUid() ?: return

        // ✅ UI 즉시 반영(optimistic update)
        val current = favoriteIdsSnapshot().toMutableSet()
        if (newValue) current.add(storeId) else current.remove(storeId)
        _favoriteIdsState.value = UiState.Success(current)

        val stores = (allStoresState.value as? UiState.Success)?.data.orEmpty()
        _favoriteStoresState.value = UiState.Success(stores.filter { current.contains(it.storeId) })

        // ✅ 서버 반영
        viewModelScope.launch {
            runCatching { favoriteRepository.setFavorite(uid, storeId, newValue) }
                .onFailure {
                    // 실패하면 다시 서버 기준으로 리로드(간단하게)
                    loadAllStoresAndFavorites()
                }
        }
    }
}
