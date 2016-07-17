package tw.kaneshih.simpletool.view;

import android.util.Log;

public class Logcat {
    private static final String TAG = "simpletool";

    private static boolean isDebug = false;

    /**
     * default is false
     *
     * @param debug
     */
    public static void enableDebug(boolean debug) {
        isDebug = debug;
    }

    private static String convertNull(String s) {
        if (s == null) {
            return "null";
        } else {
            return s;
        }
    }

    static void v(String tag, String msg) {
        Log.v(TAG, convertNull(tag) + ": " + convertNull(msg));
    }

    static void i(String tag, String msg) {
        Log.i(TAG, convertNull(tag) + ": " + convertNull(msg));
    }

    static void d(String tag, String msg) {
        if (isDebug)
            Log.d(TAG, convertNull(tag) + ": " + convertNull(msg));
    }

    static void w(String tag, String msg) {
        Log.w(TAG, convertNull(tag) + ": " + convertNull(msg));
    }

    static void e(String tag, String msg) {
        Log.e(TAG, convertNull(tag) + ": " + convertNull(msg));
    }

    static void e(String tag, String msg, Throwable e) {
        Log.e(TAG, convertNull(tag) + ": " + convertNull(msg), e);
    }

    static void wtf(String tag, String msg) {
        Log.wtf(TAG, convertNull(tag) + ": " + convertNull(msg));
    }
}
