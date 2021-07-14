package gortea.jgmax.pomodoro.timer

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

    fun start() {
        listener?.onStart(currentTime)

        isRunning = true

        scope?.launch(Dispatchers.Default) {
            run()
            if (isRunning) {
                scope.launch(Dispatchers.Main) {
                    stop(true)
                }
            }
        }
    }

    private suspend fun run() {
        setCurrentTime(currentTime)
        var currentTime = startTime
        while (currentTime > 0 && isRunning) {
            delay(INTERVAL / 2)
            currentTime = startTime - (System.currentTimeMillis() - startSystemTime) / INTERVAL
            currentTime.coerceAtLeast(0L)

            if (isRunning) {
                scope?.launch(Dispatchers.Main) {
                    updateCurrentTime(currentTime)
                }
            }
        }
    }

    fun setCurrentTime(currentTime: Long) {
        startTime = currentTime
        startSystemTime = System.currentTimeMillis()
    }

    @CallSuper
    protected open fun updateCurrentTime(currentTime: Long) {
        listener?.onTimeChanged(currentTime)
    }

    private fun stop(isEnded: Boolean) {
        isRunning = false
        listener?.onStop(isEnded)
    }

    fun stop() {
        stop(false)
    }

    interface TimeChangeListener {
        fun onStart(currentTime: Long)
        fun onTimeChanged(currentTime: Long)
        fun onStop(isEnded: Boolean)
    }
}