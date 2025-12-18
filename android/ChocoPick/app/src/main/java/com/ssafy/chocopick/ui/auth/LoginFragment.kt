package com.ssafy.chocopick.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.viewModels
import com.ssafy.chocopick.R
import com.ssafy.chocopick.databinding.FragmentLoginBinding
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pw = binding.etPassword.text?.toString().orEmpty()
            authViewModel.login(email, pw)
        }

        binding.btnGoSignUp.setOnClickListener {
            // TODO: 회원가입 화면으로 이동 (NavController 쓰면 navigate)
            // findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
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
                            binding.btnLogin.isEnabled = true
                        }
                        is UiState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                        }
                        is UiState.Success -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(requireContext(), "로그인 성공 uid=${state.data}", Toast.LENGTH_SHORT).show()

                            authViewModel.resetState()

                            // TODO: Home 이동
                            // findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                        is UiState.Error -> {
                            binding.progress.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
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
