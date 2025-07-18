package com.example.chronopanthers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class TimerManager {
    private static TimerManager instance = new TimerManager();

    private Timeline timeline;
    public int workTime = 25 * 60;
    public int breakTime = 5 * 60;
    public int timeLeft = workTime;
    public boolean isWorkTime = true;
    private boolean isRunning = false;

    private Runnable onTickCallback;
    private Runnable onModeSwitchCallback;

    private TimerManager() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public static TimerManager getInstance() {
        return instance;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            timeline.play();
        }
    }

    public void pause() {
        if (isRunning) {
            isRunning = false;
            timeline.pause();
        }
    }

    public void reset() {
        pause();
        isWorkTime = true;
        timeLeft = workTime;
        if (onTickCallback != null) {
            onTickCallback.run();
        }
    }

    public void updateDurations(int newWorkTime, int newBreakTime) {
        this.workTime = newWorkTime;
        this.breakTime = newBreakTime;

        // Reset the current time to new work time
        this.timeLeft = isWorkTime ? workTime : breakTime;
        if (onTickCallback != null) {
            onTickCallback.run();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void tick() {
        timeLeft--;

        if (timeLeft < 0) {
            // Switch modes
            isWorkTime = !isWorkTime;
            timeLeft = isWorkTime ? workTime : breakTime;

            // Notify controller to update mode UI & save session
            if (onModeSwitchCallback != null) {
                onModeSwitchCallback.run();
            }
        }

        // Notify tick update
        if (onTickCallback != null) {
            onTickCallback.run();
        }
    }

    public void setOnTick(Runnable callback) {
        this.onTickCallback = callback;
    }

    public void setOnModeSwitch(Runnable callback) {
        this.onModeSwitchCallback = callback;
    }
}