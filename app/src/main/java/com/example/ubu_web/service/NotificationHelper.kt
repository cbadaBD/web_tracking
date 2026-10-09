package com.example.ubu_web.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ubu_web.MainActivity
import com.example.ubu_web.util.Constants

/**
 * Helper para crear el canal de notificación y la notificación
 * persistente que muestra el servicio en primer plano.
 */
object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Rastreo de ubicación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación persistente del servicio de rastreo GPS"
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context, pendingCount: Int = 0): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (pendingCount > 0) {
            "Rastreando ubicación · $pendingCount puntos pendientes"
        } else {
            "Rastreando ubicación en segundo plano"
        }

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Tracking System")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun updateNotification(context: Context, pendingCount: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(Constants.NOTIFICATION_ID, buildNotification(context, pendingCount))
    }
}