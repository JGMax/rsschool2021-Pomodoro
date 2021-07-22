package gortea.jgmax.pomodoro.presenters

interface NotificationSender {
    fun <T> notifyUser(message: T)
}
