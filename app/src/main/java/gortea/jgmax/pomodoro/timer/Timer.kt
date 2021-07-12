package gortea.jgmax.pomodoro.timer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import gortea.jgmax.pomodoro.models.TimerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Timer(val model: TimerModel, private val lifecycleOwner: LifecycleOwner?) {
    var listener: TimeChangeListener? = null
    private var isRunning = true

    fun start() {
        listener?.onStart()

        isRunning = true

        lifecycleOwner?.lifecycleScope?.launch(Dispatchers.Default) {
            run()
            if (isRunning) {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    stop(true)
                }
            }
        }
    }

    private suspend fun run() {
        val startSystemTime = System.currentTimeMillis()
        val startTime = model.currentTime
        var currentTime = startTime
        while (currentTime > 0 && isRunning) {
            delay(INTERVAL / 2)
            currentTime = startTime - (System.currentTimeMillis() - startSystemTime) / INTERVAL
            currentTime.coerceAtLeast(0L)

            if (isRunning) {
                lifecycleOwner?.lifecycleScope?.launch(Dispatchers.Main) {
                    updateCurrentTime(currentTime)
                }
            }
        }
    }

    private fun updateCurrentTime(currentTime: Long) {
        model.currentTime = currentTime
        listener?.onTimeChanged(currentTime, model.progress)
    }

    private fun stop(isEnded: Boolean) {
        isRunning = false
        listener?.onStop(isEnded)
    }

    fun stop() {
        stop(false)
    }

    private companion object {
        private const val INTERVAL = 1000L
    }

    interface TimeChangeListener {
        fun onStart()
        fun onTimeChanged(currentTime: Long, progress: Int)
        fun onStop(isEnded: Boolean)
    }
}