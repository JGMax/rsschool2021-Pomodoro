package gortea.jgmax.pomodoro.models

data class TimerModel(
    val startTime: Long,
    var currentTime: Long = startTime,
    var isActive: Boolean = false,
    val id: Int = 0
) {
    val progress: Int
    get() = (((startTime - currentTime) / startTime.toFloat()) * 100).toInt()
}
