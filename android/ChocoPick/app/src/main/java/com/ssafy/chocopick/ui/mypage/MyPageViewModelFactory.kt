package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AuthRepositoryImpl
import com.ssafy.chocopick.data.repository.RewardRepositoryImpl
import com.ssafy.chocopick.data.repository.UserRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.auth.FirebaseAuthDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.RewardDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.UserDataSource

class MyPageViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {

            val authRepo = AuthRepositoryImpl(
                authDataSource = FirebaseAuthDataSource(),
                userDataSource = UserDataSource(),
            )

            val userRepo = UserRepositoryImpl(UserDataSource())

            val rewardRepo = RewardRepositoryImpl(RewardDataSource())
            @Suppress("UNCHECKED_CAST")
            return MyPageViewModel(authRepo, userRepo, rewardRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
