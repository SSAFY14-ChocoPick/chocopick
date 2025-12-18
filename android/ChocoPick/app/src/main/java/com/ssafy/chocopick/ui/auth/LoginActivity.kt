package com.ssafy.chocopick.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.databinding.ActivityLoginBinding
import com.ssafy.chocopick.MainActivity
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pw = binding.etPassword.text?.toString().orEmpty()
            authViewModel.login(email, pw)
        }

        binding.btnGoSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        collectAuthState()
    }

    private fun collectAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                        }

                        is UiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                        }

                        is UiState.Success -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인 성공",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish() // 🔥 로그인 화면 제거
                        }

                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(
                                this@LoginActivity,
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
