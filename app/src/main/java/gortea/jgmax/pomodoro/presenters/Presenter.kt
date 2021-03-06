package gortea.jgmax.pomodoro.presenters

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.adapters.TimerListAdapter
import gortea.jgmax.pomodoro.constants.CURRENT_ID_KEY
import gortea.jgmax.pomodoro.constants.CURRENT_TIME_KEY
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.receivers.Receiver
import gortea.jgmax.pomodoro.timer.Timer
import gortea.jgmax.pomodoro.timer.TimerStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

object Presenter : TimerStateObserver, LifecycleObserver {
    private val timer: Timer =
        Timer(
            -1,
            -1L,
            CoroutineScope(Dispatchers.Default)
        ).also { it.attachObserver(this) }

    private var sender: NotificationSender? = null
    private var register: ReceiverRegister? = null

    private val receiver = Receiver { _, data ->
        if (data == null) return@Receiver
        val currentId = data.extras?.getInt(CURRENT_ID_KEY) ?: return@Receiver
        val currentTime = data.extras?.getLong(CURRENT_TIME_KEY) ?: return@Receiver

        if (!timer.isStarted()) {
            setCurrentData(currentId, currentTime)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun registerLocalReceiver() {
        register?.registerLocalReceiver(receiver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun unregisterLocalReceiver() {
        register?.unregisterLocalReceiver(receiver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun clearRegister() {
        register = null
    }

    fun attachRegister(register: ReceiverRegister) {
        this.register = register
    }

    fun getCurrentTime() = timer.getCurrentTime()

    fun detachAll() {
        detachAll(Any::class)
        sender = null
        register = null
    }

    fun detachAll(type: KClass<*>) {
        timer.detachAll(type)
    }

    fun attachNotificationSender(sender: NotificationSender) {
        this.sender = sender
    }

    fun detachNotificationSender(sender: NotificationSender) {
        if (this.sender === sender) {
            this.sender = null
        }
    }

    fun attachTimerObserver(observer: TimerStateObserver) {
        timer.attachObserver(observer)
    }

    fun detachTimerObserver(observer: TimerStateObserver) {
        timer.detachObserver(observer)
    }

    fun addTimer(time: Long, adapter: RecyclerView.Adapter<*>?) {
        if (!validateTime(time)) {
            sender?.notifyUser(R.string.empty_timer_alert)
            return
        }
        val mAdapter = adapter as? TimerListAdapter ?: return
        mAdapter.add(TimerModel(time))
    }

    private fun validateTime(currentTime: Long): Boolean = currentTime > 0L

    fun getId() = timer.id

    fun isActive() = timer.isStarted()

    fun startTimer(id: Int, currentTime: Long) {
        if (timer.id == id && timer.isStarted()) return
        stopTimer()
        timer.id = id
        timer.setCurrentTime(currentTime)
        timer.start()
    }

    fun stopTimer() {
        if (timer.isStarted()) {
            timer.stop()
        }
    }

    fun stopTimer(id: Int): Boolean {
        if (timer.id == id && timer.isStarted()) {
            timer.stop()
            return true
        }
        return false
    }

    private fun setCurrentData(id: Int, currentTime: Long) {
        timer.id = id
        timer.setCurrentTime(currentTime)
    }

    fun setCurrentTime(id: Int, currentTime: Long) {
        if (timer.id == id) {
            timer.setCurrentTime(currentTime)
        }
    }

    override fun onStop(currentTime: Long) {
        if (currentTime > 0L) return
        sender?.notifyUser(R.string.timer_ended_notification)
    }
}