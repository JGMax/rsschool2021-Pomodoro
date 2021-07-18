package gortea.jgmax.pomodoro.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalDividers(
    private val divider: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val oneSideInnerDivider = divider / 2

        with(outRect) {
            top = oneSideInnerDivider
            bottom = oneSideInnerDivider
        }
    }
}