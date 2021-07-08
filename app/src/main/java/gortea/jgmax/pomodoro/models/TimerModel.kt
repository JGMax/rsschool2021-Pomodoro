package gortea.jgmax.pomodoro.models

data class TimerModel(val startTime: Int, var currentTime: Int, var isActive: Boolean)