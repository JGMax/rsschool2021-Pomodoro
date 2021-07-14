package gortea.jgmax.pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import gortea.jgmax.pomodoro.adapters.TimerListAdapter
import gortea.jgmax.pomodoro.constants.*
import gortea.jgmax.pomodoro.databinding.ActivityMainBinding
import gortea.jgmax.pomodoro.dummy.DummyContent
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.preferences.AppPreferences
import gortea.jgmax.pomodoro.services.TimerService

class MainActivity : AppCompatActivity(), LifecycleObserver, LifecycleOwner {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
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

    private fun restoreTimersList() : List<TimerModel>? {
        val appPreferences = AppPreferences(this)
        return appPreferences.getList(TIMERS_LIST_KEY)
    }

    private fun saveTimers(adapter: TimerListAdapter) {
        val appPreferences = AppPreferences(this)
        appPreferences.putList(TIMERS_LIST_KEY, adapter.timers)
        Log.e("timers", "saved")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackgroundActivity() {
        val adapter = binding.timerList.adapter as? TimerListAdapter ?: return
        saveTimers(adapter)

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
        val resultIntent = createPendingResult(PENDING_REQUEST_CODE, Intent(), 0)
        intent.putExtra(RESULTS_INTENT_KEY, resultIntent)
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PENDING_REQUEST_CODE && resultCode == STATUS_CANCELLED) {
            val currentId = data?.extras?.getInt(CURRENT_ID_KEY) ?: return
            val currentTime = data.extras?.getLong(CURRENT_TIME_KEY) ?: return

            val adapter = binding.timerList.adapter as? TimerListAdapter ?: return
            if (adapter.getCurrentTime() ?: 0L > currentTime) {
                adapter.updateTime(currentId, currentTime)
            }
        }
    }

    private companion object {
        private const val PENDING_REQUEST_CODE = 2222
        private const val TIMERS_LIST_KEY = "TIMERS_LIST"
    }
}