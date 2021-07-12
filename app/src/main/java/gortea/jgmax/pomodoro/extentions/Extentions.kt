package gortea.jgmax.pomodoro.extentions

fun Long.displayTime(): String {
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