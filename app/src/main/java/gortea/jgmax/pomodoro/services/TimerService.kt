package gortea.jgmax.pomodoro.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.timer.Timer
import gortea.jgmax.pomodoro.utils.*
import kotlinx.coroutines.GlobalScope

class TimerService : Service() {

    private var isServiceStarted = false

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
        //todo swipe notification when timer ends

        timer?.stop()
        timer = null
        try {
            stopForeground(removeNotification)
            if (removeNotification) stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun start(currentTime: Long) {
        if (isServiceStarted) return

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
        createChannel(
            this,
            notificationManager,
            NOTIFICATION_SOUND_URI
        )
        startForeground(NOTIFICATION_ID, getNotification("content", builder))
    }

    private fun startTimer(currentTime: Long) {
        timer?.stop()
        timer = Timer(currentTime, GlobalScope)
        timer?.listener = getTimerListener()
        timer?.start()
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

        override fun onStop(isEnded: Boolean) {
            if (isEnded) {
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
                stop()
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
        super.onDestroy()
    }
}