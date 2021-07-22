package gortea.jgmax.pomodoro.timer

import android.util.Log
import androidx.annotation.CallSuper

interface TimerObservable {
    val observers: MutableList<TimerStateObserver>

    @CallSuper
    fun sendNewCallback(state: TimerState) {
        observers.forEach {
            when (state) {
                is TimerState.Started -> it.onStart(state.currentTime)
                is TimerState.Changed -> it.onTimeChanged(state.currentTime)
                is TimerState.Stopped -> it.onStop(state.currentTime)
            }
        }
    }

    @CallSuper
    fun attachObserver(observer: TimerStateObserver) {
        if (!observers.contains(observer))
            observers.add(observer)
    }

    @CallSuper
    fun detachObserver(observer: TimerStateObserver) {
        if (observers.contains(observer))
            observers.remove(observer)
    }
}