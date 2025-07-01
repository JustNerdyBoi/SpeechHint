package ru.application.speechhint.ui.layouts;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WordWallLayoutManager extends RecyclerView.LayoutManager {

    private int totalHeight = 0;
    private int scrollOffsetY = 0;

    private static final double EXTRA_LINE_SPACE = 1.0;
    private static final double LINE_SPACE = 0.7;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = dy;

        if (scrollOffsetY + delta < -getHeight() / 2) {
            delta = -scrollOffsetY - getHeight() / 2;
        }
        if (scrollOffsetY + delta > totalHeight - getHeight() / 2) {
            delta = Math.max(totalHeight - getHeight() / 2 - scrollOffsetY, 0);
        }

        scrollOffsetY += delta;
        offsetChildrenVertical(-delta);
        return delta;
    }

    @Override
    public void onLayoutChildren(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state) {
        scrollOffsetY = Math.min(scrollOffsetY, Math.max(0, totalHeight - getHeight()));
        detachAndScrapAttachedViews(recycler);
        fill(recycler);
    }

    private void fill(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);

        int width = getWidth();
        int curLeft = getPaddingLeft();
        int curTop = getPaddingTop() - scrollOffsetY;
        int maxHeightInLine = 0;

        int itemCount = getItemCount();

        totalHeight = 0;

        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);

            addView(view);
            measureChildWithMargins(view, 0, 0);

            int w = getDecoratedMeasuredWidth(view);
            int h = getDecoratedMeasuredHeight(view);

            if (view.getVisibility() == View.GONE) {
                if (maxHeightInLine != 0) {
                    curTop += (int) (maxHeightInLine * EXTRA_LINE_SPACE);
                    totalHeight += (int) (maxHeightInLine * EXTRA_LINE_SPACE);
                    maxHeightInLine = 0;
                }
                curLeft = getPaddingLeft();
                continue;
            }

            if (curLeft + w > width - getPaddingRight()) {
                curLeft = getPaddingLeft();
                curTop += (int) (maxHeightInLine * LINE_SPACE);
                totalHeight += (int) (maxHeightInLine * LINE_SPACE);
                maxHeightInLine = 0;
            }

            layoutDecorated(view, curLeft, curTop, curLeft + w, curTop + h);

            curLeft += w;
            maxHeightInLine = Math.max(maxHeightInLine, h);
        }
        if (maxHeightInLine != 0) {
            totalHeight += maxHeightInLine;
        }

        if (totalHeight < getHeight()) {
            scrollOffsetY = 0;
        }
    }


    @Override
    public void scrollToPosition(int position) { // TODO: Заглушка
        scrollOffsetY = 0;
        requestLayout();
    }
}
