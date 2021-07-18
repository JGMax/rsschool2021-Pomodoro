package gortea.jgmax.pomodoro.models

import gortea.jgmax.pomodoro.extentions.displayTime


data class TimerModel(
    val startTime: Long,
    var isActive: Boolean = false,
    val id: Int = 0
) {
    var currentTime: Long = startTime
    set(value) {
        field = value
        displayTime = value.displayTime()
        progress = (((startTime - value) / startTime.toFloat()) * 100).toInt()
    }
    var progress = (((startTime - currentTime) / startTime.toFloat()) * 100).toInt()
    var displayTime = currentTime.displayTime()
}
