package gortea.jgmax.pomodoro.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import gortea.jgmax.pomodoro.R

class ProgressPie @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var max = 100
    private var min = 0
    private var progress = 0

    private var reversed = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        var color = Color.RED
        var strokeWidth = 1f
        var style = Style.FILL.value
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressPie,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.ProgressPie_color, color)
            style = styledAttrs.getInt(R.styleable.ProgressPie_view_style, style)
            max = styledAttrs.getInt(R.styleable.ProgressPie_max_progress, max)
            min = styledAttrs.getInt(R.styleable.ProgressPie_min_progress, min)
            progress = styledAttrs.getInt(R.styleable.ProgressPie_progress, min)
            reversed = styledAttrs.getBoolean(R.styleable.ProgressPie_reversed, reversed)
            strokeWidth = styledAttrs.getFloat(R.styleable.ProgressPie_stroke_width, strokeWidth)
            styledAttrs.recycle()
        }

        paint.color = color
        paint.style = if (style == Style.STROKE.value) Paint.Style.STROKE else Paint.Style.FILL
        paint.strokeWidth = strokeWidth
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

    fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setStyle(style: Style) {
        paint.style = if (style == Style.STROKE) Paint.Style.STROKE else Paint.Style.FILL
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
        invalidate()
    }

    fun setReversed(reversed: Boolean) {
        this.reversed = reversed
        invalidate()
    }

    enum class Style(val value: Int) {
        FILL(0),
        STROKE(1);
    }
}