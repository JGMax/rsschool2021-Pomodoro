package gortea.jgmax.pomodoro.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.timer.Timer
import gortea.jgmax.pomodoro.utils.*
import kotlinx.coroutines.GlobalScope

class TimerService : Service() {

    private var isServiceStarted = false
    private var isServiceForegroundStarted = false

    private var notificationManager: NotificationManager? = null
    private val builder by lazy {
        getNotificationBuilder(this)
    }
    private var timer: Timer? = null
    private var currentId: Int? = null
    private var currentTime: Long? = null

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
            COMMAND_START -> {
                currentTime = intent?.extras?.getLong(CURRENT_TIME_KEY) ?: return
                currentId = intent.extras?.getInt(CURRENT_ID_KEY) ?: return
                start(requireNotNull(currentTime))
            }
            COMMAND_STOP -> stop()
            INVALID -> return
        }
    }

    private fun stop(removeNotification: Boolean = true) {
        if (!isServiceStarted) return

        stopTimer()
        try {
            stopForeground(removeNotification)
            if (removeNotification) {
                stopSelf()
                isServiceStarted = false
            }
        } finally {
            isServiceForegroundStarted = false
        }
    }

    private fun start(currentTime: Long) {
        if (isServiceForegroundStarted) return

        try {
            if (!isServiceStarted) {
                moveToStartedState()
                isServiceStarted = true
            }
            startAndNotify()
            startTimer(currentTime)
        } finally {
            isServiceForegroundStarted = true
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

    private fun startTimer(currentTime: Long) {
        stopTimer()
        timer = Timer(currentTime, GlobalScope)
        timer?.listener = getTimerListener()
        timer?.start()
    }

    private fun stopTimer() {
        timer?.stop()
        timer = null
    }

    private fun getTimerListener() = object : Timer.TimeChangeListener {
        override fun onStart(currentTime: Long) {
            if (isServiceStarted) {
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(currentTime.displayTime(), builder)
                )
            }
        }

        override fun onTimeChanged(currentTime: Long) {
            this@TimerService.currentTime = currentTime
            if (isServiceStarted) {
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(currentTime.displayTime(), builder)
                )
            }
        }

        override fun onStop(currentTime: Long) {
            if (currentTime == 0L) {
                timer = null
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        getString(R.string.timer_ended_notification),
                        builder,
                        withSound = true,
                        flags = Notification.FLAG_AUTO_CANCEL
                    )
                )
                showToast(R.string.timer_ended_notification, this@TimerService)
                stop(false)
            }
        }
    }

    private fun sendLocalBroadcast() {
        val dataIntent = Intent(RESULT_INTENT_FILTER)
        dataIntent.apply {
            putExtra(CURRENT_ID_KEY, currentId)
            putExtra(CURRENT_TIME_KEY, currentTime)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent)
    }

    override fun onDestroy() {
        sendLocalBroadcast()
        Log.e("Service", "Destroyed")
        super.onDestroy()
    }
}