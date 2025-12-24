package com.ssafy.chocopick.ui.chatbot

import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.chocopick.ai.Helper
import com.ssafy.chocopick.databinding.FragmentChatbotBinding

class ChatBotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        adapter.submit(ChatMessage("안녕하세요 😊 무엇을 도와드릴까요?", false))

        Helper.initialize(
            requireContext(),
            onReady = {
                adapter.submit(ChatMessage(it, false))
                scroll()
            },
            onError = {
                adapter.submit(ChatMessage("⚠️ $it", false))
                scroll()
            }
        )

        binding.btnSend.setOnClickListener { send() }
    }

    private fun send() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // 사용자 메시지 추가
        adapter.submit(ChatMessage(text, true))
        binding.etMessage.setText("")
        scroll()
        hideKeyboard()

        if (!Helper.isReady()) {
            adapter.submit(ChatMessage("⏳ 모델 준비 중이에요.", false))
            scroll()
            return
        }

        // ✅ history 제거, userInput만 전달
        Helper.chat(
            userInput = text,
            onResult = { reply ->
                requireActivity().runOnUiThread {
                    adapter.submit(ChatMessage(reply, false))
                    scroll()
                }
            },
            onError = { err ->
                requireActivity().runOnUiThread {
                    adapter.submit(ChatMessage("⚠️ $err", false))
                    scroll()
                }
            }
        )
    }


    private fun scroll() {
        binding.rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    private fun hideKeyboard() {
        requireContext().getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Helper.close()
        _binding = null
    }
}
