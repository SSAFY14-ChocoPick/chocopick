package com.ssafy.chocopick.data.source.firebase.messaging

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.chocopick.R

class MyFirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    class MyFirebaseMessagingService : FirebaseMessagingService() {

        override fun onMessageReceived(message: RemoteMessage) {
            // ✅ data 우선, 없으면 notification fallback
            val title = message.data["title"]
                ?: message.notification?.title
                ?: "ChocoPick"

            val body = message.data["body"]
                ?: message.notification?.body
                ?: ""

            showNotification(title, body)
        }

        private fun showNotification(title: String, body: String) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val noti = NotificationCompat.Builder(this, "chocopick_fcm_v2")
                .setSmallIcon(R.drawable.chocopick_logo) // 반드시 존재하는 아이콘
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            // ✅ 3개 연속이면 id를 다르게 줘야 각각 뜸
            val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            nm.notify(id, noti)
        }

        override fun onNewToken(token: String) {
            // 필요하면 서버에 저장
            // Log.d("FCM", "newToken=$token")
        }
    }override fun onMessageReceived(message: com.google.firebase.messaging.RemoteMessage) {
        val title = message.notification?.title ?: "알림"
        val body = message.notification?.body ?: ""

        NotificationHelper.show(this, title, body)
    }
}