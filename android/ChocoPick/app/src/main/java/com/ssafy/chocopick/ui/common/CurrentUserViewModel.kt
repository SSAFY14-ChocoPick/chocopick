package com.ssafy.chocopick.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.User
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.UserRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CurrentUserViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState

    fun loadMe() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _userState.value = UiState.Error("로그인 필요")
            return
        }

        _userState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { userRepository.getUser(uid) }
                .onSuccess { user ->
                    if (user == null) {
                        _userState.value = UiState.Error("유저 정보 없음")
                    } else {
                        _userState.value = UiState.Success(user)
                    }
                }
                .onFailure {
                    _userState.value =
                        UiState.Error(it.message ?: "유저 로드 실패", it)
                }
        }
    }

    fun getUid(): String =
        (userState.value as? UiState.Success)?.data?.uid.orEmpty()

    fun getNickname(): String =
        (userState.value as? UiState.Success)?.data?.nickname.orEmpty()
}
