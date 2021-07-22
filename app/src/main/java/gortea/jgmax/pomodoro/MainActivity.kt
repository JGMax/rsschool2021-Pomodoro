package gortea.jgmax.pomodoro

import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import gortea.jgmax.pomodoro.adapters.TimerListAdapter
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.databinding.ActivityMainBinding
import gortea.jgmax.pomodoro.decorators.HorizontalDividers
import gortea.jgmax.pomodoro.decorators.VerticalDividers
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.preferences.AppPreferences
import gortea.jgmax.pomodoro.presenters.NotificationSender
import gortea.jgmax.pomodoro.presenters.Presenter
import gortea.jgmax.pomodoro.presenters.ReceiverRegister
import gortea.jgmax.pomodoro.receivers.Receiver
import gortea.jgmax.pomodoro.services.TimerService
import gortea.jgmax.pomodoro.utils.*

class MainActivity : AppCompatActivity(), LifecycleObserver, LifecycleOwner,
    NotificationSender, ReceiverRegister {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val presenter = Presenter

    init {
        Log.e("presenterActivity", presenter.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(this)
        lifecycle.addObserver(presenter)

        presenter.attachRegister(this)
        presenter.detachAll(TimerListAdapter.ItemViewHolder::class)
        with(binding) {
            setContentView(root)
            addBtn.setOnClickListener { onAddClick() }
        }
        setupRecyclerView()
    }

    private fun onAddClick() {
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val timeSeconds = minute * 60L + hourOfDay * 3600L
            presenter.addTimer(timeSeconds, binding.timerList.adapter)
        }

        val dialog = TimePickerDialog(
            this,
            listener,
            DEFAULT_HOUR,
            DEFAULT_MINUTE,
            true
        )
        val buttonColorValue = TypedValue()
        theme.resolveAttribute(R.attr.colorSecondary, buttonColorValue, true)

        dialog.apply {
            show()
            getButton(TimePickerDialog.BUTTON_POSITIVE)?.setTextColor(buttonColorValue.data)
            getButton(TimePickerDialog.BUTTON_NEGATIVE)?.setTextColor(buttonColorValue.data)
            getButton(TimePickerDialog.BUTTON_NEUTRAL)?.setTextColor(buttonColorValue.data)
        }
    }

    private fun setupRecyclerView() {
        binding.timerList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            val data = restoreTimersList() ?: arrayListOf()
            adapter = TimerListAdapter(data)
            addItemDecoration(
                HorizontalDividers(
                    ITEM_HORIZONTAL_DIVIDER_DP.toPx(context)
                )
            )
            addItemDecoration(
                VerticalDividers(
                    ITEM_VERTICAL_INNER_DIVIDER_DP.toPx(context)
                )
            )
        }
    }

    private fun restoreTimersList(): List<TimerModel>? {
        val appPreferences = AppPreferences(this)
        return appPreferences.getList(TIMERS_LIST_KEY)
    }

    private fun saveTimersList(list: List<TimerModel>) {
        val appPreferences = AppPreferences(this)
        appPreferences.putList(TIMERS_LIST_KEY, list)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackgroundActivity() {
        presenter.detachNotificationSender(this)

        val adapter = binding.timerList.adapter as? TimerListAdapter ?: return
        saveTimersList(adapter.getDataList())

        if (!presenter.isActive()) return

        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(COMMAND_ID, COMMAND_START)
        startService(intent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForegroundActivity() {
        presenter.attachNotificationSender(this)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancelAll()

        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(intent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearTrash() {
        presenter.detachAll(Any::class)
    }

    override fun <T> notifyUser(message: T) {
        when (message) {
            is String -> showToast(message, this)
            is Int -> showToast(message, this)
        }
    }

    private companion object {
        private const val TIMERS_LIST_KEY = "TIMERS_LIST"
    }

    override fun registerLocalReceiver(receiver: Receiver) {
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter(RESULT_INTENT_FILTER))
    }

    override fun unregisterLocalReceiver(receiver: Receiver) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}
