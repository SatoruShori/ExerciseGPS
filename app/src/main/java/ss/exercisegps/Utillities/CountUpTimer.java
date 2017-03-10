package ss.exercisegps.Utillities;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Simple timer class which count up until stopped.
 * Inspired by {@link android.os.CountDownTimer}
 */
public abstract class CountUpTimer {

    private final long interval;
    private long base, timer = 0, elapsedTime;

    public CountUpTimer(long interval) {
        this.interval = interval;
    }

    public void start() {
        base = SystemClock.elapsedRealtime();
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void stop() {
        handler.removeMessages(MSG);
        timer += elapsedTime;
    }

    public void reset() {
        synchronized (this) {
            timer = 0;
            base = SystemClock.elapsedRealtime();
        }
    }

    abstract public void onTick(long elapsedTime);

    private static final int MSG = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CountUpTimer.this) {
                elapsedTime = SystemClock.elapsedRealtime() - base;
                onTick(elapsedTime + timer);
                sendMessageDelayed(obtainMessage(MSG), interval);
            }
        }
    };
}
