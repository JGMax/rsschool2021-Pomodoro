package gortea.jgmax.pomodoro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import gortea.jgmax.pomodoro.adapters.TimerListAdapter
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.databinding.ActivityMainBinding
import gortea.jgmax.pomodoro.dummy.DummyContent
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.preferences.AppPreferences
import gortea.jgmax.pomodoro.receivers.Receiver
import gortea.jgmax.pomodoro.services.TimerService

class MainActivity : AppCompatActivity(), LifecycleObserver, LifecycleOwner {

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
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        DummyContent.build(10)
        binding.timerList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            val data = restoreTimersList() ?: DummyContent.ITEMS
            adapter = TimerListAdapter(data, this@MainActivity)
        }
    }

    private fun restoreTimersList(): List<TimerModel>? {
        val appPreferences = AppPreferences(this)
        return appPreferences.getList(TIMERS_LIST_KEY)
    }

    private fun saveTimersList(adapter: TimerListAdapter) {
        val appPreferences = AppPreferences(this)
        appPreferences.putList(TIMERS_LIST_KEY, adapter.timers)
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
        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(intent)
    }

    private companion object {
        private const val TIMERS_LIST_KEY = "TIMERS_LIST"
    }
}