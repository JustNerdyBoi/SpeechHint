package ru.application.speechhint.ui.animator;

import android.animation.ValueAnimator;
import android.view.Choreographer;
import android.view.animation.LinearInterpolator;
import androidx.recyclerview.widget.RecyclerView;

public class AutoScroller {
    private final RecyclerView recyclerView;
    private boolean running = false;
    private float currentSpeedPxPerSec = 0f;
    private float targetSpeedPxPerSec = 0f;
    private float scrollRemainder = 0f;
    private long lastFrameTimeNanos = 0L;
    private ValueAnimator speedAnimator;

    public AutoScroller(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    // Запуск прокрутки с постоянной скоростью
    public void startScrolling(float speedPxPerSecond) {
        stopScrolling();
        this.currentSpeedPxPerSec = speedPxPerSecond;
        this.targetSpeedPxPerSec = speedPxPerSecond;
        scrollRemainder = 0f;
        running = true;
        lastFrameTimeNanos = 0L; // сбросить, чтобы корректно вычислить dt на первом кадре
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    // Остановка
    public void stopScrolling() {
        running = false;
        Choreographer.getInstance().removeFrameCallback(frameCallback);
        if (speedAnimator != null) speedAnimator.cancel();
        scrollRemainder = 0f;
    }

    // Плавное изменение скорости
    public void setSpeed(float newSpeedPxPerSecond) {
        if (targetSpeedPxPerSec == newSpeedPxPerSecond) return;

        final float fromSpeed = currentSpeedPxPerSec;
        targetSpeedPxPerSec = newSpeedPxPerSecond;

        if (speedAnimator != null) speedAnimator.cancel();

        speedAnimator = ValueAnimator.ofFloat(fromSpeed, newSpeedPxPerSecond);
        speedAnimator.setDuration(1000); // 1.0 сек на изменение скорости разгон
        speedAnimator.setInterpolator(new LinearInterpolator());
        speedAnimator.addUpdateListener(animation -> {
            currentSpeedPxPerSec = (float) animation.getAnimatedValue();
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
            float dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f; // наносекунды -> секунды
            lastFrameTimeNanos = frameTimeNanos;

            float delta = currentSpeedPxPerSec * dt + scrollRemainder;
            int dy = (int) delta;
            scrollRemainder = delta - dy;

            if (dy != 0) {
                recyclerView.scrollBy(0, dy);
            }

            Choreographer.getInstance().postFrameCallback(this);
        }
    };
}
