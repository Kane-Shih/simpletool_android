package tw.kaneshih.simpletool.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import tw.kaneshih.simpletool.utility.TextUtil;
import tw.kaneshih.simpletool.utility.Validator;

/**
 * Caution! This is not accurate.
 */
public class CountDownView extends TextView {
    private static final String TAG = "CountDownView";
    private String overText = "";

    private int second;
    private boolean isCountingDown = false;
    private OnTimesUpListener listener;
    private Runnable updateSecondRunnable = new Runnable() {
        @Override
        public void run() {
            if (second <= 0) {
                setText(overText);
                if (listener != null && isCountingDown) {
                    isCountingDown = false;
                    listener.onTimesUp();
                }
            } else {
                displayTime(second);
                if (--second >= 0) {
                    postDelayed(this, 1000);
                }
            }
        }
    };

    public interface OnTimesUpListener {
        void onTimesUp();
    }

    public CountDownView(Context context) {
        super(context);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnTimesUpListener(OnTimesUpListener listener) {
        this.listener = listener;
    }

    public boolean isTimeUp() {
        return second <= 0;
    }

    public int getTimeLeft() {
        return second;
    }

    public void startCountDown(int second) {
        if (second <= 0) {
            return;
        }

        Logcat.d(TAG, "start from " + second + " sec");
        this.second = second;
        isCountingDown = true;
        updateSecondRunnable.run();
    }

    public void setTextForTimesUp(String s) {
        if (Validator.isNull(s)) {
            this.overText = "";
        } else {
            this.overText = s;
        }
    }

    public boolean isCountingDown() {
        return isCountingDown;
    }

    /**
     * Caution! This is not accurate, because our unit of time is second.
     *
     * @return
     */
    public int pause() {
        if (isCountingDown && second > 0) {
            Logcat.d(TAG, "pause at " + second + " sec");
            isCountingDown = false;
            removeCallbacks(updateSecondRunnable);
        }
        return second;
    }

    /**
     * Caution! This is not accurate, because our unit of time is second.
     *
     * @return
     */
    public int resume() {
        if (!isCountingDown && second > 0) {
            Logcat.d(TAG, "resume at " + second + "sec");
            removeCallbacks(updateSecondRunnable);
            isCountingDown = true;
            postDelayed(updateSecondRunnable, 1000);
        }
        return second;
    }

    private void displayTime(int sec) {
        setText(TextUtil.convertSecondToHHMMSS(sec));
    }

    @Override
    protected void onDetachedFromWindow() {
        pause();
        super.onDetachedFromWindow();
    }
}
