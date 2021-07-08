package gortea.jgmax.pomodoro.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.annotation.AttrRes
import gortea.jgmax.pomodoro.R

class SimpleCircle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CustomView(context, attrs, defStyleAttr) {

    private var radius = -1f
    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SimpleCircle,
                defStyleAttr,
                0
            )
            radius = styledAttrs.getFloat(R.styleable.SimpleCircle_radius, radius)
        }
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

    fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    override fun getBaseline(): Int {
        return layoutParams.height / 2
    }
}