package gortea.jgmax.pomodoro.views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import gortea.jgmax.pomodoro.R

open class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        var color = Color.RED
        var strokeWidth = 1f
        var style = Style.FILL.value
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomView,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.CustomView_draw_color, color)
            style = styledAttrs.getInt(R.styleable.CustomView_draw_style, style)
            strokeWidth = styledAttrs.getFloat(R.styleable.CustomView_stroke_width, strokeWidth)

            styledAttrs.recycle()
        }

        paint.color = color
        paint.style = if (style == Style.STROKE.value) Paint.Style.STROKE else Paint.Style.FILL
        paint.strokeWidth = strokeWidth
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

    enum class Style(val value: Int) {
        FILL(0),
        STROKE(1);
    }
}