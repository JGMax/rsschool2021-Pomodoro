package gortea.jgmax.pomodoro.models

data class TimerModel(
    val id: Int,
    val startTime: Long,
    var currentTime: Long = startTime,
    var isActive: Boolean = false
)
