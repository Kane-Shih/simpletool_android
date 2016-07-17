package tw.kaneshih.simpletool.utility;

import java.io.File;
import java.lang.ref.WeakReference;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;

public final class SetStateDrawableTask extends AsyncTask<Void, Void, StateListDrawable> {
    private WeakReference<View> viewRef;
    private String remoteUrlNormal;
    private String remoteUrlPressed;
    private String remoteUrlFocused;
    private File localFileNormal;
    private File localFilePressed;
    private File localFileFocused;

    /**
     * All arguments can not be null, and URL cannot be empty, otherwise throws
     * IllegalArgumentException.
     *
     * @param view
     * @param remoteUrlNormal
     * @param remoteUrlPressed
     * @param remoteUrlFocused
     * @param localFileNormal
     * @param localFilePressed
     * @param localFileFocused
     */
    public SetStateDrawableTask(View view, String remoteUrlNormal, String remoteUrlPressed, String remoteUrlFocused,
                                File localFileNormal, File localFilePressed, File localFileFocused) {
        if (Validator.isNull(view) || Validator.isEmpty(remoteUrlNormal) || Validator.isEmpty(remoteUrlPressed)
                || Validator.isEmpty(remoteUrlFocused) || Validator.isNull(localFileNormal)
                || Validator.isNull(localFilePressed) || Validator.isNull(localFileFocused)) {
            throw new IllegalArgumentException("SetStateDrawableTask: invalid argument");
        }
        this.viewRef = new WeakReference<>(view);
        this.remoteUrlNormal = remoteUrlNormal;
        this.remoteUrlPressed = remoteUrlPressed;
        this.remoteUrlFocused = remoteUrlFocused;
        this.localFileNormal = localFileNormal;
        this.localFilePressed = localFilePressed;
        this.localFileFocused = localFileFocused;
    }

    @Override
    protected StateListDrawable doInBackground(Void... params) {
        if (prepareFile(remoteUrlNormal, localFileNormal) && prepareFile(remoteUrlPressed, localFilePressed)
                && prepareFile(remoteUrlFocused, localFileFocused)) {
            try {
                StateListDrawable states = new StateListDrawable();
                addStateByFile(states, android.R.attr.state_pressed, localFilePressed);
                addStateByFile(states, android.R.attr.state_focused, localFileFocused);
                addStateByFile(states, 0, localFileNormal);
                return states;
            } catch (Throwable e) {
                e.printStackTrace();
                Logcat.e("SetStateDrawableTask", "preparing files failed");
                return null;
            }
        } else {
            Logcat.e("SetStateDrawableTask", "preparing files failed");
            return null;
        }
    }

    private void addStateByFile(StateListDrawable states, int state, File file) {
        if (file != null && file.exists()) {
            states.addState(
                    state != 0 ? new int[]{state} : new int[]{},
                    Drawable.createFromPath(file.getAbsolutePath()));
        }
    }

    private boolean prepareFile(String url, File local) {
        if (local != null && local.exists()) {
            return true;
        } else {
            return HttpUtil.getFileFromUrl(url, local);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(StateListDrawable states) {
        try {
            if (states != null) {
                View view = viewRef.get();
                if (view != null) {
                    if (Build.VERSION.SDK_INT >= 16) {
                        view.setBackground(states);
                    } else {
                        view.setBackgroundDrawable(states);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
