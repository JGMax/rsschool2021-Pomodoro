package gortea.jgmax.pomodoro.timer

import gortea.jgmax.pomodoro.models.TimerModel

class Timer(val model: TimerModel) {
    var listener: TimeChangeListener? = null

    fun start() {
        listener?.onStart()
        //todo start function
    }

    private fun updateCurrentTime(currentTime: Long) {
        model.currentTime = currentTime
        listener?.onTimeChange(currentTime, model.progress)
    }

    fun stop() {
        listener?.onStop(false)
        //todo stop function
    }

    interface TimeChangeListener {
        fun onStart()
        fun onTimeChange(currentTime: Long, progress: Int)
        fun onStop(isEnded: Boolean)
    }
}