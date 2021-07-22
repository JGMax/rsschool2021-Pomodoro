package gortea.jgmax.pomodoro.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import gortea.jgmax.pomodoro.MainActivity
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.constants.CHANNEL_ID


fun createChannel(context: Context, manager: NotificationManager?, soundUri: Uri? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelName = context.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
        if (soundUri != null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            channel.setSound(soundUri, audioAttributes)
        }
        manager?.createNotificationChannel(channel)
    }
}

fun getPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
}

fun getNotification(
    content: String,
    builder: NotificationCompat.Builder,
    withSound: Boolean = false,
    flags: Int = Notification.FLAG_ONGOING_EVENT
): Notification {
    val notification = builder
        .setContentText(content)
        .setSilent(!withSound)
        .setDefaults(Notification.DEFAULT_SOUND)
        .build()
    notification.flags = notification.flags or flags
    return notification
}

fun getNotificationBuilder(context: Context) =
    NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setGroup(context.getString(R.string.timer_notification_group_title))
        .setGroupSummary(false)
        .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(getPendingIntent(context))
        .setSmallIcon(R.drawable.ic_tomato)

fun showToast(@StringRes stringId: Int, context: Context) {
    Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
}

fun showToast(message: String, context: Context) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
