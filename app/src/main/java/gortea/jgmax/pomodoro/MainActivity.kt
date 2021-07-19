package gortea.jgmax.pomodoro

import android.app.Notification
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
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
import gortea.jgmax.pomodoro.receivers.Receiver
import gortea.jgmax.pomodoro.services.TimerService
import gortea.jgmax.pomodoro.utils.*

class MainActivity : AppCompatActivity(), LifecycleObserver, LifecycleOwner,
    TimerListAdapter.TimerEventsListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val receiver = Receiver { _, data ->
        if (data == null) return@Receiver
        val currentId = data.extras?.getInt(CURRENT_ID_KEY) ?: return@Receiver
        val currentTime = data.extras?.getLong(CURRENT_TIME_KEY) ?: return@Receiver

        val adapter = binding.timerList.adapter as? TimerListAdapter ?: return@Receiver
        if (adapter.getCurrentTime() ?: 0L > currentTime) {
            adapter.updateTime(currentId, currentTime)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        with(binding) {
            setContentView(root)
            addBtn.setOnClickListener { onAddClick() }
        }
        setupRecyclerView()
    }

    private fun onAddClick() {
        val adapter = binding.timerList.adapter as? TimerListAdapter ?: return
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val startTimeSeconds = minute * 60L + hourOfDay * 3600L
            if (startTimeSeconds == 0L) {
                showToast(R.string.empty_timer_alert, this)
            } else {
                adapter.add(TimerModel(startTimeSeconds))
            }
        }

        val dialog = TimePickerDialog(this, listener, 0, 5, true)
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
            adapter = TimerListAdapter(
                data,
                timerEventsListener = this@MainActivity
            )
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

    private fun saveTimersList(adapter: TimerListAdapter) {
        val appPreferences = AppPreferences(this)
        appPreferences.putList(TIMERS_LIST_KEY, adapter.getDataList())
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun registerLocalReceiver() {
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter(RESULT_INTENT_FILTER))
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun unregisterLocalReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackgroundActivity() {
        val adapter = binding.timerList.adapter as? TimerListAdapter ?: return
        saveTimersList(adapter)

        val id = adapter.getCurrentId() ?: return
        val time = adapter.getCurrentTime() ?: return

        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(COMMAND_ID, COMMAND_START)
        intent.putExtra(CURRENT_TIME_KEY, time)
        intent.putExtra(CURRENT_ID_KEY, id)
        startService(intent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForegroundActivity() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancelAll()

        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(intent)
    }

    override fun onStop(item: TimerModel, currentTime: Long) {
        if (currentTime == 0L) {
            showToast(R.string.timer_ended_notification, this)
        }
    }

    private companion object {
        private const val TIMERS_LIST_KEY = "TIMERS_LIST"
    }
}