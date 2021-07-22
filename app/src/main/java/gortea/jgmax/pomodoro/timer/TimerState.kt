package gortea.jgmax.pomodoro.timer

sealed class TimerState(val currentTime: Long) {
    class Started(currentTime: Long) : TimerState(currentTime)
    class Changed(currentTime: Long) : TimerState(currentTime)
    class Stopped(currentTime: Long) : TimerState(currentTime)
}
