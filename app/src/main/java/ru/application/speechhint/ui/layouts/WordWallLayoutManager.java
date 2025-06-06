package ru.application.speechhint.ui.layouts;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WordWallLayoutManager extends RecyclerView.LayoutManager {

    private int totalHeight = 0;      // Общая высота контента
    private int scrollOffsetY = 0;    // Текущее смещение

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
        boolean firstLine = true;

        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);

            addView(view);
            measureChildWithMargins(view, 0, 0);

            int w = getDecoratedMeasuredWidth(view);
            int h = getDecoratedMeasuredHeight(view);

            // Принудительный перенос строки, если слово == "\n"
            if (view.getVisibility() == View.GONE) {
                if (!firstLine) {
                    curTop += maxHeightInLine;
                    totalHeight += maxHeightInLine;
                } else {
                    firstLine = false;
                }
                curLeft = getPaddingLeft();
                maxHeightInLine = 0;
                continue;
            }

            // Если не помещается — перенос строки
            if (curLeft + w > width - getPaddingRight()) {
                curLeft = getPaddingLeft();
                curTop += maxHeightInLine;
                totalHeight += maxHeightInLine;
                maxHeightInLine = 0;
            }

            layoutDecorated(view, curLeft, curTop, curLeft + w, curTop + h);

            curLeft += w;
            maxHeightInLine = Math.max(maxHeightInLine, h);
        }
        // Добавляем высоту последней строки
        totalHeight += maxHeightInLine;

        // Если контент меньше высоты экрана — не даём скроллить вниз
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
