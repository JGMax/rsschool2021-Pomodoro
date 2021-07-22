package gortea.jgmax.pomodoro.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.presenters.NotificationSender
import gortea.jgmax.pomodoro.presenters.Presenter
import gortea.jgmax.pomodoro.timer.TimerStateObserver
import gortea.jgmax.pomodoro.utils.*

class TimerService : Service(), TimerStateObserver, NotificationSender {

    private var isServiceStarted = false

    private var notificationManager: NotificationManager? = null
    private val builder by lazy {
        getNotificationBuilder(this)
    }

    private val presenter = Presenter

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

    private fun processIntent(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> start()
            COMMAND_STOP -> stop()
            INVALID -> return
        }
    }

    private fun stop() {
        if (!isServiceStarted) return

        try {
            stopForeground(true)
            stopSelf()
        } finally {
            presenter.detachNotificationSender(this)
            presenter.detachTimerObserver(this)
            isServiceStarted = false
        }
    }

    private fun start() {
        if (isServiceStarted) return

        try {
            moveToStartedState()
            startAndNotify()
        } finally {
            presenter.attachNotificationSender(this)
            presenter.attachTimerObserver(this)
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
        createChannel(
            this,
            notificationManager,
            NOTIFICATION_SOUND_URI
        )
        startForeground(NOTIFICATION_ID, getNotification("content", builder))
    }

    override fun onDestroy() {
        Log.e("Service", "Destroyed")
        presenter.detachNotificationSender(this)
        presenter.detachTimerObserver(this)
        super.onDestroy()
    }

    override fun onStart(currentTime: Long) {
        if (isServiceStarted) {
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(currentTime.displayTime(), builder)
            )
        }
    }

    override fun onTimeChanged(currentTime: Long) {
        if (isServiceStarted) {
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(currentTime.displayTime(), builder)
            )
        }
    }

    override fun onStop(currentTime: Long) {
        if (isServiceStarted) {
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(
                    getString(R.string.timer_ended_notification),
                    builder,
                    withSound = true,
                    flags = Notification.FLAG_AUTO_CANCEL
                )
            )
        }
    }

    override fun <T> notifyUser(message: T) {
        when (message) {
            is String -> showToast(message, this)
            is Int -> showToast(message, this)
        }
    }
}