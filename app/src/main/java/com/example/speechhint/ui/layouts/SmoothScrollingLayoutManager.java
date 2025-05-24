package com.example.speechhint.ui.layouts;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SmoothScrollingLayoutManager extends FlowLayoutManager {
    private static final float MILLISECONDS_PER_INCH = 150f;

    public SmoothScrollingLayoutManager(Context context) {
        super();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            @Override
            public View findViewByPosition(int targetPosition) {
                return SmoothScrollingLayoutManager.this.findViewByPosition(targetPosition);
            }
        };
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public void timedScrollToPosition(RecyclerView recyclerView, int position, long duration) {
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return duration / (float) findViewByPosition(position).getBottom();
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            @Override
            public View findViewByPosition(int targetPosition) {
                return SmoothScrollingLayoutManager.this.findViewByPosition(targetPosition);
            }
        };
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }
} 