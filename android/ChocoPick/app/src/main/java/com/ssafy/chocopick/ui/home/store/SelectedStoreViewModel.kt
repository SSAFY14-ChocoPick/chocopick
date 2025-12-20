package com.ssafy.chocopick.ui.home.store

import androidx.lifecycle.ViewModel
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.repository.SelectedStoreRepository
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SelectedStoreViewModel(
    private val repo : SelectedStoreRepository
) : ViewModel() {
    private val _selectedStore = MutableStateFlow<Store?>(repo.getSelectedStore())
    val selectedStore : StateFlow<Store?> = _selectedStore

    fun select(store : Store){
        repo.selectStore(store)
        _selectedStore.value = store
    }

    fun clear(){
        repo.clear()
        _selectedStore.value = null
    }
}