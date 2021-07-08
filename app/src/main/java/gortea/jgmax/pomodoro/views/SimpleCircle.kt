package gortea.jgmax.pomodoro.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import gortea.jgmax.pomodoro.R

class SimpleCircle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius = -1f

    init {
        var color = Color.RED
        var strokeWidth = 4f
        var style = Style.FILL.value
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SimpleCircle,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.CustomView_color, color)
            strokeWidth = styledAttrs.getFloat(R.styleable.CustomView_stroke_width, strokeWidth)
            style = styledAttrs.getInt(R.styleable.CustomView_view_style, style)

            radius = styledAttrs.getFloat(R.styleable.SimpleCircle_radius, radius)
        }

        paint.strokeWidth = strokeWidth
        paint.color = color
        paint.style = if (style == Style.STROKE.value) Paint.Style.STROKE else Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                if (radius < 0) height.coerceAtMost(width).toFloat() / 2 else radius,
                paint
            )
        }
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

    fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    override fun getBaseline(): Int {
        return layoutParams.height / 2
    }

    enum class Style(val value: Int) {
        FILL(0),
        STROKE(1);
    }
}