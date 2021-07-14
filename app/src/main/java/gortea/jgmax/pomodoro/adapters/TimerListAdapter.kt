package gortea.jgmax.pomodoro.adapters

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.button.MaterialButton
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.databinding.TimerItemBinding
import gortea.jgmax.pomodoro.extentions.displayTime
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.timer.LifecycleTimer
import gortea.jgmax.pomodoro.timer.Timer

class TimerListAdapter(
    val timers: List<TimerModel>,
    private val context: Context
) : RecyclerView.Adapter<TimerListAdapter.ViewHolder>() {

    private var timer: LifecycleTimer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timers[position], position)
    }

    override fun getItemCount(): Int = timers.size

    fun getCurrentTime(): Long? = timer?.model?.currentTime
    fun getCurrentId(): Int? = timer?.model?.id

    fun updateTime(id: Int, time: Long) {
        val model = timers.find { it.id == id } ?: return
        model.currentTime = time
        timer?.setCurrentTime(model.currentTime)
    }

    fun add(item: TimerModel): Boolean {
        var id = -1
        timers.forEachIndexed { i, model ->
            if (i != timers.lastIndex) {
                if (model.id + 1 != timers[i + 1].id) {
                    id = model.id + 1
                    return@forEachIndexed
                }
            }
        }
        if (id == -1) {
            if (timers.size == Int.MAX_VALUE) {
                return false
            }
            //timers.add(item.copy(id = timers.lastIndex))
        } else {
            //timers.add(item.copy(id = id))
        }
        notifyItemInserted(timers.lastIndex)
        notifyItemRangeChanged(timers.lastIndex, itemCount)
        return true
    }

    inner class ViewHolder(private val binding: TimerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimerModel, position: Int) {
            with(binding) {
                timerTv.text = item.currentTime.displayTime()

                val timerListener = getTimerListener(item, binding)
                startStopBtn.setOnClickListener { onStartStopClick(item, timerListener) }
                resetBtn.setOnClickListener { onResetClick(it, item, binding) }
                deleteBtn.setOnClickListener { onDeleteClick(item, position) }

                progressPie.setProgress(item.progress)
                activeMonitor(item, indicator, startStopBtn, timerListener)
            }
        }

        private fun activeMonitor(
            item: TimerModel,
            indicator: ImageView,
            startStopBtn: Button,
            listener: Timer.TimeChangeListener?
        ) {
            setBlinking(item.isActive, indicator)
            if (item.isActive) {
                if (timer == null) {
                    startTimer(item, listener)
                }
                startStopBtn.text = context.getString(R.string.stop_btn)
            } else {
                startStopBtn.text = if (item.currentTime == 0L) {
                    context.getString(R.string.restart_btn)
                } else {
                    context.getString(R.string.start_btn)
                }
            }
        }

        private fun getTimerListener(
            item: TimerModel,
            binding: TimerItemBinding
        ) = object : Timer.TimeChangeListener {
            override fun onStart(currentTime: Long) {
                item.isActive = true
                with(binding) {
                    setBlinking(true, indicator, true)
                    startStopBtn.text = context.getString(R.string.stop_btn)
                    progressPie.setProgress(item.progress)
                    timerTv.text = currentTime.displayTime()
                }
            }

            override fun onTimeChanged(currentTime: Long) {
                with(binding) {
                    progressPie.setProgress(item.progress)
                    timerTv.text = currentTime.displayTime()
                }
            }

            override fun onStop(isEnded: Boolean) {
                item.isActive = false
                with(binding) {
                    setBlinking(false, indicator)
                    timer = null
                    startStopBtn.text = if (isEnded) {
                        context.getString(R.string.restart_btn)
                    } else {
                        context.getString(R.string.start_btn)
                    }
                }
            }
        }

        private fun setBlinking(isActive: Boolean, indicator: ImageView, force: Boolean = false) {
            fun startBlinking(view: ImageView) {
                if (!view.isVisible || force) {
                    view.isVisible = true
                    (view.background as? AnimationDrawable)?.start()
                }
            }

            fun stopBlinking(view: ImageView) {
                view.isVisible = false
                (view.background as? AnimationDrawable)?.stop()
            }

            if (isActive) {
                startBlinking(indicator)
            } else {
                stopBlinking(indicator)
            }
        }

        private fun onStartStopClick(item: TimerModel, listener: Timer.TimeChangeListener?) {
            if (item.currentTime == 0L && !item.isActive) {
                item.currentTime = item.startTime
            }

            if (timer?.model != item) {
                startTimer(item, listener)
            } else {
                stopTimer()
            }
        }

        private fun startTimer(item: TimerModel, listener: Timer.TimeChangeListener?) {
            stopTimer()
            timer = LifecycleTimer(item, context as? LifecycleOwner)
            timer?.apply {
                this.listener = listener
                start()
            }
        }

        private fun stopTimer() {
            timer?.stop()
            timer = null
        }

        private fun onResetClick(
            view: View,
            item: TimerModel,
            binding: TimerItemBinding
        ) {
            val vectorIcon = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_reset)
            (view as? MaterialButton)?.icon = vectorIcon
            vectorIcon?.start()

            with(binding) {
                item.apply {
                    currentTime = startTime
                    if (isActive) {
                        timer?.setCurrentTime(currentTime)
                    } else {
                        timerTv.text = currentTime.displayTime()
                        startStopBtn.text = context.getString(R.string.start_btn)
                        progressPie.setProgress(progress)
                    }
                }
            }
        }

        private fun onDeleteClick(item: TimerModel, position: Int) {
            //timers.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            if (timer?.model == item) {
                stopTimer()
            }
        }
    }
}