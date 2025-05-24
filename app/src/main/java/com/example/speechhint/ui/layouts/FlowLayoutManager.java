package com.example.speechhint.ui.layouts;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class FlowLayoutManager extends RecyclerView.LayoutManager {
    private int horizontalSpacing = 4;
    private int verticalSpacing = 8;
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

            // Check if this is a newline word
            Object tag = view.getTag();

            if (tag != null && tag.equals("\n")) {
                currentX = getPaddingLeft();
                currentY += maxHeight + verticalSpacing * 3; // Add extra spacing for newlines
                maxHeight = 0;
                // Hide the newline view
                layoutDecorated(view, 0, 0, 0, 0);
                continue;
            }

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
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        int lastWordPos = getChildCount() == 0 ? 0 : getChildAt(getChildCount() - 1).getBottom() + verticalSpacing - getHeight();

        if (verticalOffset + dy < 0) {
            dy = -verticalOffset;
        } else if (lastWordPos <= 0 && dy > 0) {
            dy = lastWordPos;
        }

        offsetChildrenVertical(-dy);
        verticalOffset += dy;

        return dy;
    }
}