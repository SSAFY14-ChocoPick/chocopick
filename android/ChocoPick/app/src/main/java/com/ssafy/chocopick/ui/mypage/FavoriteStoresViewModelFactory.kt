package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.StoreRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource

class FavoriteStoresViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteStoresViewModel::class.java)) {

            // 여기서 의존성 조립
            val storeDataSource = StoreDataSource()
            val storeRepository = StoreRepositoryImpl(storeDataSource)

            @Suppress("UNCHECKED_CAST")
            return FavoriteStoresViewModel(storeRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
