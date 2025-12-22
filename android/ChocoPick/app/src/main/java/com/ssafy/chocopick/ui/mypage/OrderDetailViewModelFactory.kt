package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AuthRepositoryImpl
import com.ssafy.chocopick.data.repository.OrderRepositoryImpl
import com.ssafy.chocopick.data.repository.StoreRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.OrderDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class OrderDetailViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val authDs = FirebaseAuthDataSource()
        val userDs = UserDataSource()
        val authRepo = AuthRepositoryImpl(authDs, userDs)

        val storeDs = StoreDataSource()
        val storeRepo = StoreRepositoryImpl(storeDs)

        val orderDs = OrderDataSource()
        val orderRepo = OrderRepositoryImpl(orderDs, storeRepo)

        @Suppress("UNCHECKED_CAST")
        return OrderDetailViewModel(authRepo, orderRepo, storeRepo) as T
    }
}
