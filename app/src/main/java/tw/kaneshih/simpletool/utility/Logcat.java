package tw.kaneshih.simpletool.utility;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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

    public static void memInfo() {
        try {
            final Runtime runtime = Runtime.getRuntime();
            final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
            if (maxHeapSizeInMB - usedMemInMB >= 10) {
                Log.i(TAG, "APP HEAP(MB) - used/max: " + usedMemInMB + "/" + maxHeapSizeInMB);
            } else {
                Log.e(TAG, "DANGER!! APP HEAP(MB) - used/max: " + usedMemInMB + "/" + maxHeapSizeInMB);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void extractLogcatToFile(Context context, File target) {
        try {
            Date now = new Date();
            Process process = Runtime.getRuntime().exec("logcat -d -v time");
            FileUtil.writeStringInputStreamToFile(process.getInputStream(), target, false, null, "\n---START " + now + "---\n",
                    "\n---END " + now + "---\n");

            String info = "App: " + context.getPackageName() + "\n";
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                if (pkgInfo != null) {
                    info += "Version: " + pkgInfo.versionName + "(" + pkgInfo.versionCode + ")\n";
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            info += DeviceInfoUtil.getDeviceInfo(context);
            FileUtil.writeToFile(info, target, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
