package gortea.jgmax.pomodoro.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalDividers(
    private val innerDivider: Int,
    private val outerDivider: Int,
    private val footer: Boolean = true,
    private val header: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val adapter = parent.adapter ?: return

        val position =
            parent.getChildAdapterPosition(view).takeIf { it != RecyclerView.NO_POSITION } ?: return

        val hasPrev = position > 0
        val hasNext = position < adapter.itemCount - 1

        val oneSideInnerDivider = innerDivider / 2

        with(outRect) {
            top =
                if (hasPrev) oneSideInnerDivider else if (header) outerDivider else oneSideInnerDivider
            bottom =
                if (hasNext) oneSideInnerDivider else if (footer) outerDivider else oneSideInnerDivider
        }
    }

}