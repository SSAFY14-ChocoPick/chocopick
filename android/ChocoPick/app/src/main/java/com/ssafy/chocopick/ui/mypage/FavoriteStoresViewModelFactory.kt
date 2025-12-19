package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.*
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.FavoriteDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class FavoriteStoresViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteStoresViewModel::class.java)) {

            // Auth (너 프로젝트 AuthRepositoryImpl 생성자에 맞춰 조정)
            val authDs = FirebaseAuthDataSource()
            val userDs = UserDataSource()
            val authRepo: AuthRepository = AuthRepositoryImpl(authDs, userDs)

            // Store
            val storeRepo: StoreRepository = StoreRepositoryImpl(StoreDataSource())

            // Favorite
            val favRepo: FavoriteRepository = FavoriteRepositoryImpl(FavoriteDataSource())

            @Suppress("UNCHECKED_CAST")
            return FavoriteStoresViewModel(authRepo, storeRepo, favRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
