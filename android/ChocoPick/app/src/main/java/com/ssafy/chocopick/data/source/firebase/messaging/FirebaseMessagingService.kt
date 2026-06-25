package com.ssafy.chocopick.data.source.firebase.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        // data payload 우선, 없으면 notification fallback
        val title = message.data["title"]
            ?: message.notification?.title
            ?: "ChocoPick"

        val body = message.data["body"]
            ?: message.notification?.body
            ?: ""

        NotificationHelper.show(this, title, body)
    }
}
