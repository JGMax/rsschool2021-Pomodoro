package gortea.jgmax.pomodoro.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.annotation.AttrRes
import gortea.jgmax.pomodoro.R

class ProgressPie @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CustomView(context, attrs, defStyleAttr) {

    private var max = 100
    private var min = 0
    private var progress = 0

    private var reversed = false

    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressPie,
                defStyleAttr,
                0
            )

            max = styledAttrs.getInt(R.styleable.ProgressPie_max_progress, max)
            min = styledAttrs.getInt(R.styleable.ProgressPie_min_progress, min)
            progress = styledAttrs.getInt(R.styleable.ProgressPie_progress, min)
            reversed = styledAttrs.getBoolean(R.styleable.ProgressPie_reversed, reversed)
            styledAttrs.recycle()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (max == min || progress >= max || progress < min) return

        val step = 360 / (max - min).toFloat()
        val p = if (reversed) progress - max else progress

        val angle = p * step

        canvas?.apply {
            drawArc(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                -90f,
                angle,
                true,
                paint
            )
        }
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    fun setMax(max: Int) {
        this.max = max
        invalidate()
    }

    fun setMin(min: Int) {
        this.min = min
        invalidate()
    }

    fun setReversed(reversed: Boolean) {
        this.reversed = reversed
        invalidate()
    }

    override fun getBaseline(): Int {
        return layoutParams.height
    }
}