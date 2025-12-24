package com.ssafy.chocopick.ui.chatbot

class ChatBot(
    override var who: String = "Assistant",
    override var text: String
) : ChatText(who, text)
