package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.User
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.UserRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyPageViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState

    fun loadMyProfile() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _userState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        _userState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { userRepository.getUser(uid) }
                .onSuccess { user ->
                    if (user == null) _userState.value = UiState.Error("유저 정보를 찾을 수 없습니다.")
                    else _userState.value = UiState.Success(user)
                }
                .onFailure { e ->
                    _userState.value = UiState.Error(e.message ?: "프로필 로드 실패", e)
                }
        }
    }
}
