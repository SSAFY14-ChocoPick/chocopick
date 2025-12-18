package com.ssafy.chocopick.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("이메일/비밀번호를 입력해주세요.")
            return
        }

        _authState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { authRepository.login(email.trim(), password) }
                .onSuccess { uid -> _authState.value = UiState.Success(uid) }
                .onFailure { e -> _authState.value = UiState.Error(e.message ?: "로그인 실패", e) }
        }
    }

    fun signUp(email: String, password: String, nickname: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("이메일/비밀번호를 입력해주세요.")
            return
        }
        if (password.length < 6) {
            _authState.value = UiState.Error("비밀번호는 6자 이상이어야 합니다.")
            return
        }
        if (nickname.length > 10) {
            _authState.value = UiState.Error("닉네임은 10자 이하이어야 합니다.")
            return
        }

        _authState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { authRepository.signUp(email.trim(), password, nickname) }
                .onSuccess { uid -> _authState.value = UiState.Success(uid) }
                .onFailure { e -> _authState.value = UiState.Error(e.message ?: "회원가입 실패", e) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = UiState.Idle
        }
    }

    fun getCurrentUid(): String? = authRepository.getCurrentUid()

    fun resetState() {
        _authState.value = UiState.Idle
    }
}
