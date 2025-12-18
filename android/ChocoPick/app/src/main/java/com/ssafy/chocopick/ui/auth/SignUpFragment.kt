package com.ssafy.chocopick.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentSignUpBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSignUpBinding.bind(view)

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pw = binding.etPassword.text?.toString().orEmpty()
            authViewModel.signUp(email, pw)
        }

        binding.btnBackToLogin.setOnClickListener {
            // TODO: 로그인 화면으로 돌아가기
            // findNavController().popBackStack()
        }

        collectAuthState()
    }

    private fun collectAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                            binding.progress.visibility = View.GONE
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(requireContext(), "회원가입 성공 uid=${state.data}", Toast.LENGTH_SHORT).show()

                            authViewModel.resetState()

                            // TODO: 회원가입 후 로그인 화면 이동
                            // findNavController().popBackStack()
                        }
                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
