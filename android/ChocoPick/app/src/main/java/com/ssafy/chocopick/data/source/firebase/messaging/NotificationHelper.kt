package com.ssafy.chocopick.data.source.firebase.messaging

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ssafy.chocopick.R

object NotificationHelper {
    private const val CHANNEL_ID = "chocopick"

    fun show(context: Context, title: String, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch =
                NotificationChannel(CHANNEL_ID, "ChocoPick", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }

        val noti = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 너 프로젝트 아이콘으로 변경
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), noti)
    }
}