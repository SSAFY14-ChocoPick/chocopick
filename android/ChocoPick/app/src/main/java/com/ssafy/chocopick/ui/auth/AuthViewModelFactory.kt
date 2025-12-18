package com.ssafy.chocopick.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.AuthRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class AuthViewModelFactory : ViewModelProvider.Factory {

    private val repo: AuthRepository by lazy {
        AuthRepositoryImpl(FirebaseAuthDataSource(), UserDataSource())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
