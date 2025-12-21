package com.ssafy.chocopick.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AuthRepositoryImpl
import com.ssafy.chocopick.data.repository.UserRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class CurrentUserViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrentUserViewModel(
            authRepository = AuthRepositoryImpl(
                FirebaseAuthDataSource(),
                UserDataSource()
            ),
            userRepository = UserRepositoryImpl(UserDataSource())
        ) as T
    }
}
