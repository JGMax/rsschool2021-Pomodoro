package gortea.jgmax.pomodoro.dummy

import gortea.jgmax.pomodoro.models.TimerModel

object DummyContent {
    val ITEMS = arrayListOf<TimerModel>()

    fun build(count: Int) {
        ITEMS.clear()
        repeat(count) {
            ITEMS.add(TimerModel(it % Int.MAX_VALUE, 8125, 8125, false))
        }
    }
}