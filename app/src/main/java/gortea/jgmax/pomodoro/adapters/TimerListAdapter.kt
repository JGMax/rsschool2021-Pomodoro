package gortea.jgmax.pomodoro.adapters

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.Log
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
import gortea.jgmax.pomodoro.databinding.FooterBinding
import gortea.jgmax.pomodoro.databinding.TimerItemBinding
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.timer.LifecycleTimer
import gortea.jgmax.pomodoro.timer.Timer
import gortea.jgmax.pomodoro.utils.displayTime

enum class ItemTypes(val value: Int) {
    TIMER(0), FOOTER(1);
}

class TimerListAdapter(
    items: List<TimerModel>,
    private val context: Context,
    private val timerEventsListener: TimerEventsListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val timers = items.toMutableList()
    private var timer: LifecycleTimer? = null
    private var updateListener = false

    override fun getItemViewType(position: Int): Int {
        return if (position == timers.size) {
            ItemTypes.FOOTER.value
        } else {
            ItemTypes.TIMER.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemTypes.FOOTER.value -> {
                val footerBinding =
                    FooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                FooterViewHolder(footerBinding)
            }
            else -> {
                val itemBinding =
                    TimerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(itemBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> holder.bind(timers[position], position)
        }
    }

    override fun getItemCount(): Int = timers.size + 1

    fun getCurrentTime(): Long? = timer?.model?.currentTime
    fun getCurrentId(): Int? = timer?.model?.id

    fun updateTime(id: Int, time: Long) {
        val model = timers.find { it.id == id } ?: return
        model.currentTime = time
        timer?.setCurrentTime(model.currentTime)
    }

    fun getDataList(): List<TimerModel> = timers.toList()

    fun add(item: TimerModel): Boolean {
        fun getAvailableId(): Int {
            var id = -1
            timers.forEachIndexed { i, model ->
                if (i != timers.lastIndex) {
                    if (model.id + 1 != timers[i + 1].id) {
                        id = model.id + 1
                        return@forEachIndexed
                    }
                }
            }
            return id
        }

        var id = getAvailableId()

        if (id == -1 && timers.size == Int.MAX_VALUE) return false
        if (id == -1) id = timers.size

        val result = timers.add(item.copy(id = id))
        if (result) {
            notifyItemInserted(timers.lastIndex)
            notifyItemRangeChanged(timers.lastIndex, itemCount)
        }
        return result
    }

    fun delete(position: Int): Boolean {
        if (position !in timers.indices) return false
        val item = timers.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        if (timer?.model == item) {
            stopTimer()
        }
        return true
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

    inner class ItemViewHolder(private val binding: TimerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimerModel, position: Int) {
            with(binding) {
                timerTv.text = item.displayTime

                startStopBtn.setOnClickListener {
                    val timerListener = getTimerListener(item, binding)
                    onStartStopClick(item, timerListener)
                }

                resetBtn.setOnClickListener { onResetClick(it, item, binding) }
                deleteBtn.setOnClickListener {
                    onDeleteClick(position)
                    updateListener = true
                }

                progressPie.setProgress(item.progress)
                activeMonitor(item, indicator, startStopBtn, binding)

                if (item.isActive && updateListener) {
                    timer?.listener = getTimerListener(item, binding)
                    updateListener = false
                }
            }
        }

        private fun activeMonitor(
            item: TimerModel,
            indicator: ImageView,
            startStopBtn: Button,
            binding: TimerItemBinding
        ) {
            setBlinking(item.isActive, indicator)
            if (item.isActive) {
                if (timer == null) {
                    val listener = getTimerListener(item, binding)
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
                timerEventsListener?.onStart(item, currentTime)
            }

            override fun onTimeChanged(currentTime: Long) {
                with(binding) {
                    progressPie.setProgress(item.progress)
                    timerTv.text = currentTime.displayTime()
                }
                timerEventsListener?.onUpdate(item, currentTime)
            }

            override fun onStop(isEnded: Boolean) {
                item.isActive = false
                timer = null
                with(binding) {
                    setBlinking(false, indicator)
                    startStopBtn.text = if (isEnded) {
                        context.getString(R.string.restart_btn)
                    } else {
                        context.getString(R.string.start_btn)
                    }
                }
                timerEventsListener?.onStop(item, isEnded)
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
                    progressPie.setProgress(progress)
                    timerTv.text = currentTime.displayTime()
                    if (isActive) {
                        timer?.setCurrentTime(currentTime)
                    } else {
                        startStopBtn.text = context.getString(R.string.start_btn)
                    }
                }
            }
        }

        private fun onDeleteClick(position: Int) {
            delete(position)
        }
    }

    inner class FooterViewHolder(binding: FooterBinding) : RecyclerView.ViewHolder(binding.root)

    interface TimerEventsListener {
        fun onStart(item: TimerModel, currentTime: Long) {}
        fun onUpdate(item: TimerModel, currentTime: Long) {}
        fun onStop(item: TimerModel, isEnded: Boolean) {}
    }
}