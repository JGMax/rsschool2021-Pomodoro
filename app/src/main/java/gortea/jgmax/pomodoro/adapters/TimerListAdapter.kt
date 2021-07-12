package gortea.jgmax.pomodoro.adapters

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.button.MaterialButton
import gortea.jgmax.pomodoro.R
import gortea.jgmax.pomodoro.databinding.TimerItemBinding
import gortea.jgmax.pomodoro.extentions.displayTime
import gortea.jgmax.pomodoro.models.TimerModel
import gortea.jgmax.pomodoro.timer.Timer
import gortea.jgmax.pomodoro.views.ProgressPie

class TimerListAdapter(
    private val timers: MutableList<TimerModel>,
    private val context: Context
) : RecyclerView.Adapter<TimerListAdapter.ViewHolder>() {

    private var timer: Timer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timers[position], position)
    }

    override fun getItemCount(): Int = timers.size

    inner class ViewHolder(private val binding: TimerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimerModel, position: Int) {
            with(binding) {
                timer.text = item.currentTime.displayTime()

                startStopBtn.setOnClickListener {
                    onStartStopClick(item, object : Timer.TimeChangeListener {
                        override fun onStart() {
                            item.isActive = true
                            setBlinking(true, indicator)
                            startStopBtn.text = context.getString(R.string.stop_btn)
                        }

                        override fun onTimeChange(currentTime: Long, progress: Int) {
                            progressPie.setProgress(progress)
                            timer.text = currentTime.displayTime()
                        }

                        override fun onStop(isEnded: Boolean) {
                            item.isActive = false
                            setBlinking(false, indicator)
                            startStopBtn.text = context.getString(R.string.start_btn)
                        }
                    })
                }

                resetBtn.setOnClickListener { onResetClick(it, item, timer, progressPie) }
                deleteBtn.setOnClickListener { onDeleteClick(item, position) }

                progressPie.setProgress(item.progress)
                setBlinking(item.isActive, indicator)
                if (item.isActive) {
                    startStopBtn.text = context.getString(R.string.stop_btn)
                } else {
                    startStopBtn.text = context.getString(R.string.start_btn)
                }
            }
        }

        private fun setBlinking(isActive: Boolean, indicator: ImageView) {
            fun startBlinking(view: ImageView) {
                if (!view.isVisible) {
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

        private fun onStartStopClick(item: TimerModel, listener: Timer.TimeChangeListener) {
            fun startTimer() {
                timer?.stop()
                timer = Timer(item)
                timer?.apply {
                    this.listener = listener
                    start()
                }
            }

            if (timer?.model != item) {
                startTimer()
            } else {
                stopTimer()
            }
        }

        private fun stopTimer() {
            timer?.stop()
            timer = null
        }

        private fun onResetClick(view: View, item: TimerModel, timer: TextView, pie: ProgressPie) {
            val vectorIcon = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_reset)
            (view as? MaterialButton)?.icon = vectorIcon
            vectorIcon?.start()

            item.currentTime = item.startTime
            if (!item.isActive) {
                timer.text = item.currentTime.displayTime()
                pie.setProgress(item.progress)
            }
        }

        private fun onDeleteClick(item: TimerModel, position: Int) {
            if (timer?.model == item) {
                stopTimer()
            }
            timers.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }

        private fun add(item: TimerModel): Boolean {
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
                timers.add(item.copy(id = timers.lastIndex))
            } else {
                timers.add(item.copy(id = id))
            }
            notifyItemInserted(timers.lastIndex)
            notifyItemRangeChanged(timers.lastIndex, itemCount)
            return true
        }
    }
}