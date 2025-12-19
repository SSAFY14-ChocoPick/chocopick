package com.ssafy.chocopick.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.ssafy.chocopick.R
import com.ssafy.chocopick.data.source.firebase.realtime.RealtimePaths
import com.ssafy.chocopick.databinding.FragmentMyPageBinding
import com.ssafy.chocopick.ui.auth.AuthViewModel
import com.ssafy.chocopick.ui.auth.AuthViewModelFactory
import com.ssafy.chocopick.ui.auth.LoginActivity
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }
    private val myPageViewModel: MyPageViewModel by viewModels {
        MyPageViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectUserState()
        collectRewardState()

        myPageViewModel.loadMyProfile()
        myPageViewModel.loadReward()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            // 로그인 화면 이동
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

    }

    private fun collectUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                myPageViewModel.userState.collect { state ->
                    when(state) {
                        is UiState.Idle -> {}
                        is UiState.Loading -> { /* 로딩 표시 원하면 */ }
                        is UiState.Success -> {
                            val nickname = state.data.nickname
                            binding.tvGreeting.text =
                                if (nickname.isBlank()) "회원님 안녕하세요"
                                else "${nickname}님 안녕하세요"
                        }
                        is UiState.Error -> {
                            binding.tvGreeting.text = "회원님 안녕하세요"
                        }
                    }
                }
            }
        }
    }

    private fun collectRewardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.rewardState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            val reward = state.data
                            binding.tvMembershipGrade.text = reward.membershipTier
                            binding.progressToNext.progress =
                                (reward.stamps % 10) * 10
                            binding.tvMembershipDesc.text =
                                myPageViewModel.getBenefitText(reward.membershipTier)
                            binding.tvNextGradeHint.text =
                                "다음 등급까지 ${10 - (reward.stamps % 10)}회 남았어요"
                        }
                        else -> Unit
                    }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}