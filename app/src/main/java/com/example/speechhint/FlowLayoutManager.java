package com.example.speechhint;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class FlowLayoutManager extends RecyclerView.LayoutManager {
    private int horizontalSpacing = 4;
    private int verticalSpacing = 4;
    private int verticalOffset = 0;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }

        detachAndScrapAttachedViews(recycler);

        int width = getWidth();
        int currentX = getPaddingLeft();
        int currentY = getPaddingTop() - verticalOffset;
        int maxHeight = 0;

        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            int viewWidth = getDecoratedMeasuredWidth(view);
            int viewHeight = getDecoratedMeasuredHeight(view);

            if (currentX + viewWidth > width - getPaddingRight()) {
                currentX = getPaddingLeft();
                currentY += maxHeight + verticalSpacing;
                maxHeight = 0;
            }

            layoutDecorated(view, currentX, currentY, currentX + viewWidth, currentY + viewHeight);
            currentX += viewWidth + horizontalSpacing;
            maxHeight = Math.max(maxHeight, viewHeight);
        }
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        int scroll = dy;
        verticalOffset += scroll;
        offsetChildrenVertical(-scroll);
        return scroll;
    }
} 