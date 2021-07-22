package gortea.jgmax.pomodoro.adapters

import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.button.MaterialButton
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.databinding.FooterBinding
import gortea.jgmax.pomodoro.databinding.TimerItemBinding
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.presenters.Presenter
import gortea.jgmax.pomodoro.timer.TimerStateObserver
import gortea.jgmax.pomodoro.utils.displayTime

class TimerListAdapter(
    items: List<TimerModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val timers = items.toMutableList()
    private var updateListener = false

    private val presenter = Presenter
    private var attachedObserver: TimerStateObserver? = null

    override fun getItemViewType(position: Int): Int {
        return if (position == timers.size) {
            R.layout.footer
        } else {
            R.layout.timer_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.footer -> {
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

        if (presenter.stopTimer(item.id)) {
            detachObserver()
        }

        return true
    }

    private fun detachObserver() {
        if (attachedObserver != null) {
            presenter.detachTimerObserver(requireNotNull(attachedObserver))
        }
        (attachedObserver as? RecyclerView.ViewHolder)?.setIsRecyclable(true)
        attachedObserver = null
    }

    inner class ItemViewHolder(private val binding: TimerItemBinding) :
        RecyclerView.ViewHolder(binding.root), TimerStateObserver {
        fun bind(item: TimerModel, position: Int) {
            with(binding) {
                timerTv.text = item.displayTime

                startStopBtn.setOnClickListener { onStartStopClick(item) }

                resetBtn.setOnClickListener { onResetClick(it, item, binding) }
                deleteBtn.setOnClickListener {
                    onDeleteClick(position)
                    updateListener = true
                }

                progressPie.setProgress(item.progress, withAnimation = false)
                activeMonitor(item, indicator, startStopBtn, binding)

                if (item.isActive && updateListener) {
                    attachObserver()
                    updateListener = false
                }
            }
        }

        private fun attachObserver() {
            if (this == attachedObserver) return
            detachObserver()
            presenter.attachTimerObserver(this)
            attachedObserver = this
            setIsRecyclable(false)
        }

        private fun activeMonitor(
            item: TimerModel,
            indicator: ImageView,
            startStopBtn: Button,
            binding: TimerItemBinding
        ) {
            val context = binding.indicator.context
            setBlinking(item.isActive, indicator)
            startStopBtn.text = when {
                item.isActive && item.currentTime != 0L -> {
                    attachObserver()
                    presenter.startTimer(item.id, item.currentTime)
                    context.getString(R.string.stop_btn)
                }
                item.currentTime == 0L -> {
                    item.isActive = false
                    context.getString(R.string.restart_btn)
                }
                else -> {
                    item.isActive = false
                    context.getString(R.string.start_btn)
                }
            }
        }

        private fun setBlinking(isActive: Boolean, indicator: ImageView) {
            fun startBlinking(view: ImageView) {
                val anim = (view.background as? AnimationDrawable) ?: return
                if (!anim.isRunning) {
                    view.isVisible = true
                    anim.start()
                }
            }

            fun stopBlinking(view: ImageView) {
                view.isVisible = false
                val anim = (view.background as? AnimationDrawable) ?: return
                if (anim.isRunning) {
                    anim.stop()
                }
            }

            if (isActive) {
                startBlinking(indicator)
            } else {
                stopBlinking(indicator)
            }
        }

        private fun onStartStopClick(item: TimerModel) {
            if (item.currentTime == 0L && !item.isActive) {
                item.currentTime = item.startTime
            }

            if (presenter.getId() != item.id && presenter.getId() != -1) {
                presenter.stopTimer()
                detachObserver()
                attachObserver()
                presenter.startTimer(item.id, item.currentTime)
            } else if (!presenter.isActive()) {
                attachObserver()
                presenter.startTimer(item.id, item.currentTime)
            } else {
                presenter.stopTimer()
                detachObserver()
            }
        }

        private fun onResetClick(
            view: View,
            item: TimerModel,
            binding: TimerItemBinding
        ) {
            val context = binding.indicator.context
            val vectorIcon = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_reset)
            (view as? MaterialButton)?.icon = vectorIcon
            vectorIcon?.start()

            with(binding) {
                item.apply {
                    currentTime = startTime
                    progressPie.setProgress(progress)
                    timerTv.text = currentTime.displayTime()
                    if (isActive) {
                        presenter.setCurrentTime(id, currentTime)
                    } else {
                        startStopBtn.text = context.getString(R.string.start_btn)
                    }
                }
            }
        }

        private fun onDeleteClick(position: Int) {
            delete(position)
        }

        override fun onStart(currentTime: Long) {
            val item = timers.find { it.id == presenter.getId() } ?: return
            item.currentTime = currentTime
            item.isActive = true
            Log.e("started", this.toString())
            with(binding) {
                val context = indicator.context
                setBlinking(true, indicator)
                startStopBtn.text = context.getString(R.string.stop_btn)
                progressPie.setProgress(item.progress)
                timerTv.text = currentTime.displayTime()
            }
        }

        override fun onTimeChanged(currentTime: Long) {
            val item = timers.find { it.id == presenter.getId() } ?: return
            item.currentTime = currentTime
            Log.e("item", item.toString() + item.currentTime.toString())
            Log.e("changed", this.toString())
            with(binding) {
                progressPie.setProgress(item.progress)
                timerTv.text = currentTime.displayTime()
            }
        }

        override fun onStop(currentTime: Long) {
            val item = timers.find { it.id == presenter.getId() } ?: return
            item.currentTime = currentTime
            item.isActive = false
            Log.e("stopped", this.toString())
            with(binding) {
                val context = indicator.context
                setBlinking(false, indicator)
                startStopBtn.text = if (currentTime == 0L) {
                    context.getString(R.string.restart_btn)
                } else {
                    context.getString(R.string.start_btn)
                }
                progressPie.setProgress(item.progress)
                timerTv.text = currentTime.displayTime()
            }
        }
    }

    inner class FooterViewHolder(binding: FooterBinding) : RecyclerView.ViewHolder(binding.root)
}