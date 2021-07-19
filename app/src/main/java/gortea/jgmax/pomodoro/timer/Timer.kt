package gortea.jgmax.pomodoro.timer

import android.os.SystemClock.uptimeMillis
import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class Timer(
    private val currentTime: Long,
    private val scope: CoroutineScope?,
    private val INTERVAL: Long = 1000L
) {
    var listener: TimeChangeListener? = null
    private var isRunning = true
    private var startTime = currentTime
    private var startSystemTime = 0L
    private var currentTimerTime = startTime

    fun start() {
        listener?.onStart(currentTime)

        isRunning = true

        scope?.launch(Dispatchers.Default) {
            run()
            if (isRunning) {
                scope.launch(Dispatchers.Main) {
                    stop()
                }
            }
        }
    }

    private suspend fun run() {
        setCurrentTime(currentTime)
        currentTimerTime = startTime
        while (currentTimerTime > 0 && isRunning) {
            delay(INTERVAL / 2)
            currentTimerTime = startTime - (uptimeMillis() - startSystemTime) / INTERVAL
            currentTimerTime.coerceAtLeast(0L)

            if (isRunning) {
                scope?.launch(Dispatchers.Main) {
                    updateCurrentTime(currentTimerTime)
                }
            }
        }
    }

    fun setCurrentTime(currentTime: Long) {
        startTime = currentTime
        startSystemTime = uptimeMillis()
        currentTimerTime = currentTime
        if (currentTime == 0L) {
            stop()
        }
    }

    @CallSuper
    protected open fun updateCurrentTime(currentTime: Long) {
        listener?.onTimeChanged(currentTime)
    }

    @CallSuper
    open fun stop() {
        isRunning = false
        listener?.onStop(currentTimerTime)
    }

    interface TimeChangeListener {
        fun onStart(currentTime: Long)
        fun onTimeChanged(currentTime: Long)
        fun onStop(currentTime: Long)
    }
}