package com.ssafy.chocopick.ui.home.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.StoreRepository

class StoreMapViewModelFactory(
    private val repo: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StoreMapViewModel(repo) as T
    }
}