package gortea.jgmax.pomodoro.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import gortea.jgmax.pomodoro.MainActivity
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.extentions.displayTime
import gortea.jgmax.pomodoro.timer.Timer
import kotlinx.coroutines.GlobalScope

class TimerService : Service() {

    private companion object {
        private const val CHANNEL_ID = "Pomodoro notification"
        private const val NOTIFICATION_ID = 777
    }

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setGroup(getString(R.string.timer_notification_group_title))
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_tomato)
    }
    private var timer: Timer? = null
    private var currentId: Int? = null
    private var currentTime: Long? = null

    private var resultsIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processIntent(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun processIntent(intent: Intent?) {
        when(intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                currentTime = intent?.extras?.getLong(CURRENT_TIME_KEY) ?: return
                currentId = intent.extras?.getInt(CURRENT_ID_KEY) ?: return
                if (resultsIntent == null) {
                    resultsIntent = intent.extras?.getParcelable(RESULTS_INTENT_KEY)
                }
                start(requireNotNull(currentTime))
            }
            COMMAND_STOP -> stop()
            INVALID -> return
        }
    }

    private fun stop() {
        if (!isServiceStarted) return

        try {
            timer?.stop()

            timer = null
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun start(currentTime: Long) {
        if(isServiceStarted) return

        try {
            moveToStartedState()
            startAndNotify()
            startTimer(currentTime)
        } finally {
            isServiceStarted = true
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, TimerService::class.java))
        } else {
            startService(Intent(this, TimerService::class.java))
        }
    }

    private fun startAndNotify() {
        createChannel()
        startForeground(NOTIFICATION_ID, getNotification("content"))
    }

    private fun startTimer(currentTime: Long) {
        timer?.stop()
        timer = Timer(currentTime, GlobalScope)
        timer?.listener = getTimerListener()
        timer?.start()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun getTimerListener() = object : Timer.TimeChangeListener {
        override fun onStart(currentTime: Long) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification(currentTime.displayTime()))
        }

        override fun onTimeChanged(currentTime: Long) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification(currentTime.displayTime()))
        }

        override fun onStop(isEnded: Boolean) {
            notificationManager?.notify(NOTIFICATION_ID, getNotification(getString(R.string.timer_ended_notification)))
        }
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    override fun onDestroy() {
        val dataIntent = Intent()
        dataIntent.apply {
            putExtra(CURRENT_ID_KEY, currentId)
            putExtra(CURRENT_TIME_KEY, currentTime)
        }

        resultsIntent?.send(this, STATUS_CANCELLED, dataIntent)
        super.onDestroy()
    }
}