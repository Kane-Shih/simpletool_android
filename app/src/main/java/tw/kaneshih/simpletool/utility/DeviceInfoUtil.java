package tw.kaneshih.simpletool.utility;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";

    /**
     * Get device install package name
     *
     * @param context
     * @return The device install package name
     */
    public static List<String> getInstalledAppPackageNames(Context context) {

        if (Validator.isNull(context)) {
            return null;
        }

        List<String> result = new ArrayList<String>();
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        if (list != null) {
            ApplicationInfo appInfo;
            for (ResolveInfo info : list) {
                appInfo = info.activityInfo.applicationInfo;
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                    result.add(appInfo.packageName);
                }
            }
        } else {
            Logcat.e(TAG, "pkg manager returns null");
        }
        return result;
    }

    /**
     * Check app whether already install in the device or not
     *
     * @param context
     * @param packageName
     * @return Install status
     */

    public static boolean isAppInstalled(Context context, String packageName) {

        if (Validator.isNull(context) || Validator.isNull(packageName) || Validator.isEmpty(packageName)) {
            return false;
        }

        boolean isInstalled = false;
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        if (list != null) {
            ApplicationInfo appInfo;
            for (ResolveInfo info : list) {
                appInfo = info.activityInfo.applicationInfo;
                if (packageName.equals(appInfo.packageName)) {
                    isInstalled = true;
                    break;
                }
            }
        } else {
            Logcat.e(TAG, "pkg manager returns null");
        }
        Logcat.d(TAG, "isAppInstalled: " + packageName + " - " + isInstalled);
        return isInstalled;
    }

    /**
     * Get system property
     *
     * @param context
     * @param key
     * @return System property
     */
    public static String getSystemProperties(Context context, String key) {
        if (Validator.isNull(context) || Validator.isNull(key) || Validator.isEmpty(key)) {
            return null;
        }

        String ret = "";

        try {

            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class systemProperties = cl.loadClass("android.os.SystemProperties");

            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;

            @SuppressWarnings("unchecked")
            Method get = systemProperties.getMethod("get", paramTypes);

            // Parameters
            Object[] params = new Object[1];
            params[0] = key;

            ret = (String) get.invoke(systemProperties, params);

        } catch (Exception e) {
            ret = "";
        }
        return ret;
    }

    /**
     * Get device info
     *
     * @param context
     * @return deviceInfo
     */
    public static String getDeviceInfo(Context context) {
        if (Validator.isNull(context)) {
            return null;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        return "Android SDK:" + android.os.Build.VERSION.SDK_INT + ", RELEASE:" + android.os.Build.VERSION.RELEASE
                + ", MODEL:" + android.os.Build.MODEL + ", DEVICE:" + android.os.Build.DEVICE
                + ", BRAND:" + android.os.Build.BRAND + ", MANUFACTURER:" + android.os.Build.MANUFACTURER
                + ", PRODUCT:" + android.os.Build.PRODUCT
                + ", Screen[width:" + size.x + ", height:" + size.y + "]"
                + ", Display[Density:" + metrics.density + ", xdpi:" + metrics.xdpi + ", ydpi:" + metrics.ydpi
                + ", widthPixels:" + metrics.widthPixels + ", heightPixels:" + metrics.heightPixels
                + ", scaledDensity(for fonts):" + metrics.scaledDensity
                + "], Current Network Type:" + NetworkUtil.getCurrentNetworkType(context)
                + ", memInfo:" + getHeapInfo()
                + ", timezone: " + TimeZone.getDefault().getID();
    }

    /**
     * Get devices density
     *
     * @param context
     * @return device density
     */
    public static float getDevicesDensity(Context context) {
        if (Validator.isNull(context)) {
            return 0;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    /**
     * Get device info include android build version,android build model,android
     * screen size
     *
     * @param context
     * @return device info[android build version,android build model,android
     * screen size]
     */
    public static String getDeviceSimpleInfo(Context context) {

        if (Validator.isNull(context)) {
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        String releaseVersion = android.os.Build.VERSION.RELEASE;
        String modelName = android.os.Build.MODEL;
        String screenSize = size.x + "x" + size.y;
        String info = releaseVersion + " " + modelName + " " + screenSize;
        Logcat.d(TAG, info);
        return info;
    }

    /**
     * Get heap info
     *
     * @return info
     */

    public static String getHeapInfo() {
        String info = null;
        try {
            double allocated = Debug.getNativeHeapAllocatedSize() / 1048576.0;
            double available = (double) Debug.getNativeHeapSize() / 1048576.0;
            double free = (double) Debug.getNativeHeapFreeSize() / 1048576.0;
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);

            info = "heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB ("
                    + df.format(free) + "MB free)" + ", memory: allocated: "
                    + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of "
                    + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB ("
                    + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)";
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return info;
    }

    /**
     * Get device screen size
     *
     * @param context
     * @return device screen size
     */

    public static Point getScreenSize(Context context) {
        if (Validator.isNull(context)) {
            return null;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

}