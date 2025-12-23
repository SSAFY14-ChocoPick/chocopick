package com.ssafy.chocopick.ui.chatbot

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.databinding.FragmentChatbotBinding

class ChatBotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⬅ 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupChat()

        setupInput()
    }

    // -----------------------------
    // Chat setup
    // -----------------------------
    private fun setupChat() {
        chatAdapter = ChatAdapter()

        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = chatAdapter


        // 🤖 첫 AI 환영 메시지
        chatAdapter.submit(
            ChatMessage(
                "안녕하세요 😊\n초콜릿이나 매장에 대해 궁금한 점을 물어보세요!",
                false
            )
        )
    }

    // -----------------------------
    // Input & Send
    // -----------------------------
    private fun setupInput() {

        // 전송 버튼 클릭
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // 키보드 엔터로 전송
        binding.etMessage.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // 사용자 메시지
        chatAdapter.submit(ChatMessage(text, true))
        binding.etMessage.setText("")
        scrollToBottom()
        hideKeyboard()

        // 🤖 더미 AI 응답 (→ 나중에 API 연결)
        binding.rvChat.postDelayed({
            if (_binding == null) return@postDelayed

            chatAdapter.submit(
                ChatMessage("해당 매장은 선물용으로 인기가 많아요 😊", false)
            )
            scrollToBottom()
        }, 600)
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
