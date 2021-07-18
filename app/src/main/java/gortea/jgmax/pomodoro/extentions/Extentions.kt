package gortea.jgmax.pomodoro.extentions

import android.content.Context
import kotlin.math.roundToInt

fun Long.displayTime(): String {
    if (this < 0L) {
        return "00:00:00"
    }
    fun format(n: Long) = if (n < 10) {
        "0$n"
    } else {
        "$n"
    }

    val hoursPart = this / 3600
    val hours: String = format(hoursPart)
    val minutesPart = (this % 3600) / 60
    val minutes: String = format(minutesPart)
    val secondsPart = this % 60
    val seconds = format(secondsPart)
    return "$hours:$minutes:$seconds"
}

fun Int.toPx(context: Context): Int {
    val metrics = context.resources.displayMetrics
    return (this * (metrics.density)).roundToInt()
}
