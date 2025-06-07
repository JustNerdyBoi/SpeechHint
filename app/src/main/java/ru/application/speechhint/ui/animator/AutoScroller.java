package ru.application.speechhint.ui.animator;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import androidx.recyclerview.widget.RecyclerView;

public class AutoScroller {
    private final RecyclerView recyclerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    private float currentSpeedPxPerSec = 0f;
    private float targetSpeedPxPerSec = 0f;

    private long lastTickTime = 0L;
    private ValueAnimator speedAnimator;

    public AutoScroller(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    // Запуск прокрутки с постоянной скоростью
    public void startScrolling(float speedPxPerSecond) {
        stopScrolling();
        this.currentSpeedPxPerSec = speedPxPerSecond;
        this.targetSpeedPxPerSec = speedPxPerSecond;
        running = true;
        lastTickTime = System.currentTimeMillis();
        handler.post(scrollRunnable);
    }

    // Остановка
    public void stopScrolling() {
        running = false;
        handler.removeCallbacks(scrollRunnable);
        if (speedAnimator != null) speedAnimator.cancel();
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

    // Равномерная прокрутка с текущей скоростью
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            float dt = (now - lastTickTime) / 1000f;
            lastTickTime = now;

            int dy = (int) (currentSpeedPxPerSec * dt);
            if (dy != 0) {
                recyclerView.scrollBy(0, dy);
            }

            handler.postDelayed(this, 16);
        }
    };
}
