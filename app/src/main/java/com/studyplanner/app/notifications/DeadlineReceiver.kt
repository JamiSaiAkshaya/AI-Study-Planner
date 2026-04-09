package com.studyplanner.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.studyplanner.app.R

/**
 * DeadlineReceiver – BroadcastReceiver that fires a notification
 * when a task deadline is approaching.
 * Scheduled via WorkManager's DeadlineWorker.
 */
class DeadlineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra("task_title") ?: "Task Reminder"
        val message = intent.getStringExtra("task_message") ?: "You have a task due soon!"

        val nm      = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "study_planner_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channel, "Study Planner", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📚 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
