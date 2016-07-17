package tw.kaneshih.simpletool.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;

public class ApplicationUtil {
    public static String getPackageHash(Context context, String mdAlgorithm) {
        if (Validator.isEmpty(mdAlgorithm)) {
            mdAlgorithm = "SHA";
        }
        context = context.getApplicationContext();
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance(mdAlgorithm);
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {

        }
        return null;
    }

    public static String getCurrentVersionInfo(Context context) {
        context = context.getApplicationContext();
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName + "(" + info.versionCode + ")";
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentVersionName(Context context) {
        context = context.getApplicationContext();
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getCurrentVersion(Context context) {
        context = context.getApplicationContext();
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
