package com.ssafy.chocopick.data.source.firebase.messaging

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ssafy.chocopick.R

object NotificationHelper {
    private const val CHANNEL_ID = "chocopick_fcm_v2"
    private const val BEACON_CH_ID = "beacon_entry"

    fun show(context: Context, title: String, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch =
                NotificationChannel(CHANNEL_ID, "ChocoPick", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }

        val noti = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.chocopick_logo) // 너 프로젝트 아이콘으로 변경
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), noti)
    }

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                BEACON_CH_ID, "매장 입장 알림", NotificationManager.IMPORTANCE_HIGH
            )
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(ch)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEntry(ctx: Context) {
        ensureChannel(ctx)
        val n = NotificationCompat.Builder(ctx, BEACON_CH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("매장 입장 감지")
            .setContentText("비콘 1m 이내로 접근했습니다.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(ctx).notify(9001, n)
    }

}