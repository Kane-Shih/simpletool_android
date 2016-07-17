package tw.kaneshih.simpletool.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

public class UiUtil {
    private Context context;
    private ProgressDialog waiting;
    private int alertDialogStyle = 0;
    private boolean isWaiting = false;

    public UiUtil(Context context) {
        if (Validator.isNull(context)) {
            return;
        }

        this.context = context;
        this.waiting = new ProgressDialog(context);
    }

    /**
     * 
     * @param context
     * @param progressDialogStyle
     *            - 0(default) or style resource id
     * @param alertDialogStyle
     *            - 0(default) or style resource id
     */
    public UiUtil(Context context, int progressDialogStyle, int alertDialogStyle) {
        if (Validator.isNull(context)) {
            return;
        }

        this.context = context;
        if (progressDialogStyle == 0) {
            this.waiting = new ProgressDialog(context);
        } else {
            this.waiting = new ProgressDialog(context, progressDialogStyle);
        }
        this.alertDialogStyle = alertDialogStyle;
    }

    /**
     * Show progress dialog with message, we use only one progress dialog
     * instance
     * 
     * @param msg
     * @param cancelable
     */
    public void startWait(String msg, boolean cancelable) {
        if (Validator.isNull(msg)) {
            return;
        }
        if (Validator.isNull(waiting)) {
            return;
        }

        try {
            isWaiting = true;
            waiting.setMessage(msg);
            waiting.setCancelable(cancelable);
            waiting.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * dismiss progress dialog, we use only one progress dialog instance
     */
    public void endWait() {
        if (Validator.isNull(waiting)) {
            return;
        }

        try {
            isWaiting = false;
            waiting.dismiss();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isWaiting() {
        if (Validator.isNull(waiting)) {
            return false;
        }
        if (isWaiting) {
            return true;
        } else {
            return waiting.isShowing();
        }
    }

    public void showToast(String msg) {
        if (Validator.isNull(msg)) {
            return;
        }
        if (Validator.isNull(context)) {
            return;
        }

        try {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param title
     *            - can be null, which means there's no title
     * @param message
     * @param positiveButtonText
     * @param positiveListener
     * @param negativeButtonText
     *            - can be null, which means only one button
     * @param negativeListener
     *            - can be null, which means only one button
     */
    public void showConfirmDialog(CharSequence title, CharSequence message, CharSequence positiveButtonText,
            DialogInterface.OnClickListener positiveListener, CharSequence negativeButtonText,
            DialogInterface.OnClickListener negativeListener) {
        if (Validator.isNull(message)) {
            return;
        }
        if (Validator.isNull(positiveButtonText)) {
            return;
        }
        if (Validator.isNull(positiveListener)) {
            return;
        }
        if (Validator.isNull(context)) {
            return;
        }

        try {
            AlertDialog.Builder builder;
            if (alertDialogStyle == 0) {
                builder = new AlertDialog.Builder(context);
            } else {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(context, alertDialogStyle));
            }
            if (title != null) {
                builder.setTitle(title);
            }
            builder.setMessage(message);
            builder.setCancelable(false);
            builder.setPositiveButton(positiveButtonText, positiveListener);
            if (negativeButtonText != null && negativeListener != null) {
                builder.setNegativeButton(negativeButtonText, negativeListener);
            }
            builder.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param title
     *            - can be null, which means there's no title
     * @param view
     * @param positiveButtonText
     * @param positiveListener
     * @param negativeButtonText
     *            - can be null, which means only one button
     * @param negativeListener
     *            - can be null, which means only one button
     */
    public void showConfirmDialog(CharSequence title, View view, CharSequence positiveButtonText,
            DialogInterface.OnClickListener positiveListener, CharSequence negativeButtonText,
            DialogInterface.OnClickListener negativeListener) {
        if (Validator.isNull(view)) {
            return;
        }
        if (Validator.isNull(positiveButtonText)) {
            return;
        }
        if (Validator.isNull(positiveListener)) {
            return;
        }
        if (Validator.isNull(context)) {
            return;
        }

        try {
            AlertDialog.Builder builder;
            if (alertDialogStyle == 0) {
                builder = new AlertDialog.Builder(context);
            } else {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(context, alertDialogStyle));
            }
            if (title != null) {
                builder.setTitle(title);
            }
            builder.setView(view);
            builder.setCancelable(false);
            builder.setPositiveButton(positiveButtonText, positiveListener);
            if (negativeButtonText != null && negativeListener != null) {
                builder.setNegativeButton(negativeButtonText, negativeListener);
            }
            builder.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
