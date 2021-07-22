package gortea.jgmax.pomodoro.timer

import android.os.SystemClock.uptimeMillis
import android.util.Log
import androidx.annotation.CallSuper
import kotlinx.coroutines.*
import kotlin.reflect.KClass

open class Timer(
    var id: Int,
    currentTime: Long,
    private val scope: CoroutineScope?,
    private val INTERVAL: Long = 1000L
) : TimerObservable {
    override val observers: MutableList<TimerStateObserver> = mutableListOf()

    private var isRunning = false
    private var currentJob: Job? = null

    private var startTime = currentTime
    private var startSystemTime = 0L
    private var currentTimerTime = startTime

    fun detachAll(type: KClass<*>) {
        observers.removeAll { it::class == type }
    }

    fun start() {
        sendNewCallback(TimerState.Started(currentTimerTime))

        isRunning = true

        currentJob = scope?.launch(Dispatchers.Default) {
            run()
            if (isRunning) {
                scope.launch(Dispatchers.Main) {
                    stop()
                }
            }
        }
    }

    private suspend fun run() {
        setCurrentTime(currentTimerTime)
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
        if (currentTime == 0L && isRunning) {
            stop()
        }
    }

    fun isStarted() = isRunning

    override fun detachObserver(observer: TimerStateObserver) {
        super.detachObserver(observer)
        if (observers.isEmpty()) {
            stop()
        }
    }

    @CallSuper
    protected open fun updateCurrentTime(currentTime: Long) {
        sendNewCallback(TimerState.Changed(currentTime))
    }

    @CallSuper
    open fun stop() {
        isRunning = false
        currentJob?.cancel()
        Log.e("Timer", "stopped")
        sendNewCallback(TimerState.Stopped(currentTimerTime))
    }
}