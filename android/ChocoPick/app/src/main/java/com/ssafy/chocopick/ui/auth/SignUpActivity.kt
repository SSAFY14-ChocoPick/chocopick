package com.ssafy.chocopick.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.databinding.ActivitySignUpBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pw = binding.etPassword.text?.toString().orEmpty()
            authViewModel.signUp(email, pw)
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
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
                            binding.btnSignUp.isEnabled = true
                        }

                        is UiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnSignUp.isEnabled = false
                        }

                        is UiState.Success -> {
                            Toast.makeText(
                                this@SignUpActivity,
                                "회원가입 성공",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(
                                this@SignUpActivity,
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
