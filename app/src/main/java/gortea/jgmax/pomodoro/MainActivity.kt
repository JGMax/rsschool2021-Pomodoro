package gortea.jgmax.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import gortea.jgmax.pomodoro.adapters.TimerListAdapter
import gortea.jgmax.pomodoro.databinding.ActivityMainBinding
import gortea.jgmax.pomodoro.dummy.DummyContent

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        DummyContent.build(10)
        binding.timerList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TimerListAdapter(DummyContent.ITEMS, this@MainActivity)
        }
    }
}