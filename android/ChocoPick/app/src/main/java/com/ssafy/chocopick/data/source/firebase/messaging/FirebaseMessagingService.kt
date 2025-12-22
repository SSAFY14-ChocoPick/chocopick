package com.ssafy.chocopick.data.source.firebase.messaging

class MyFirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(message: com.google.firebase.messaging.RemoteMessage) {
        val title = message.notification?.title ?: "알림"
        val body = message.notification?.body ?: ""

        NotificationHelper.show(this, title, body)
    }
}