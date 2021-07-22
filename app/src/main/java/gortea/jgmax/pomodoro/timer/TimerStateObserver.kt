package gortea.jgmax.pomodoro.timer

interface TimerStateObserver {
    fun onStart(currentTime: Long) {}
    fun onTimeChanged(currentTime: Long) {}
    fun onStop(currentTime: Long) {}
}