package com.ssafy.chocopick.ui.home.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.data.repository.StoreRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource

class StoreListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo: StoreRepository = StoreRepositoryImpl(StoreDataSource())
        return StoreListViewModel(repo) as T
    }
}