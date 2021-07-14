package gortea.jgmax.pomodoro.timer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import gortea.jgmax.pomodoro.models.TimerModel

class LifecycleTimer(
    val model: TimerModel,
    lifecycleOwner: LifecycleOwner?,
    interval: Long = 1000L
) : Timer(model.currentTime, lifecycleOwner?.lifecycleScope, interval) {

    override fun updateCurrentTime(currentTime: Long) {
        model.currentTime = currentTime
        super.updateCurrentTime(currentTime)
    }
}