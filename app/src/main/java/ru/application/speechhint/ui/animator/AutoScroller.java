package ru.application.speechhint.ui.animator;

import android.animation.ValueAnimator;
import android.view.Choreographer;
import android.view.animation.LinearInterpolator;
import androidx.recyclerview.widget.RecyclerView;

public class AutoScroller {
    private final RecyclerView recyclerView;
    private final float density;
    private boolean running = false;
    private float currentSpeedDpPerSec = 0f;
    private float targetSpeedDpPerSec = 0f;
    private float scrollRemainder = 0f;
    private long lastFrameTimeNanos = 0L;
    private ValueAnimator speedAnimator;

    public AutoScroller(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.density = recyclerView.getResources().getDisplayMetrics().density;
    }

    // dp -> px
    private float dpToPx(float dp) {
        return dp * density;
    }

    // Запуск прокрутки с постоянной скоростью (в dp)
    public void startScrolling(float speedDpPerSecond) {
        stopScrolling();
        this.currentSpeedDpPerSec = speedDpPerSecond;
        this.targetSpeedDpPerSec = speedDpPerSecond;
        scrollRemainder = 0f;
        running = true;
        lastFrameTimeNanos = 0L;
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    // Остановка
    public void stopScrolling() {
        running = false;
        Choreographer.getInstance().removeFrameCallback(frameCallback);
        if (speedAnimator != null) speedAnimator.cancel();
        scrollRemainder = 0f;
    }

    // Плавное изменение скорости (в dp)
    public void setSpeed(float newSpeedDpPerSecond) {
        if (targetSpeedDpPerSec == newSpeedDpPerSecond) return;

        final float fromSpeed = currentSpeedDpPerSec;
        targetSpeedDpPerSec = newSpeedDpPerSecond;

        if (speedAnimator != null) speedAnimator.cancel();

        speedAnimator = ValueAnimator.ofFloat(fromSpeed, newSpeedDpPerSecond);
        speedAnimator.setDuration(50); // 0.05 сек
        speedAnimator.setInterpolator(new LinearInterpolator());
        speedAnimator.addUpdateListener(animation -> {
            currentSpeedDpPerSec = (float) animation.getAnimatedValue();
        });
        speedAnimator.start();
    }

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!running) return;

            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos;
            }
            float dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f;
            lastFrameTimeNanos = frameTimeNanos;

            // Перевод скорости из dp/sec в px/sec для вычисления смещения в пикселях
            float deltaPx = dpToPx(currentSpeedDpPerSec) * dt + scrollRemainder;
            int dy = (int) deltaPx;
            scrollRemainder = deltaPx - dy;

            if (dy != 0) {
                recyclerView.scrollBy(0, dy);
            }

            Choreographer.getInstance().postFrameCallback(this);
        }
    };
}
