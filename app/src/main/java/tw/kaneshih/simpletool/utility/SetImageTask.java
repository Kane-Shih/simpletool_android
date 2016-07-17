package tw.kaneshih.simpletool.utility;

import java.io.File;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

public final class SetImageTask extends AsyncTask<Void, Void, Bitmap> {
    private WeakReference<View> viewRef;
    private String remoteUrl;
    private File localFile;

    /**
     * All arguments can not be null, and URL cannot be empty, otherwise throws
     * IllegalArgumentException.
     *
     * @param view
     * @param remoteUrl
     * @param localFile
     */
    public SetImageTask(View view, String remoteUrl, File localFile) {
        if (Validator.isNull(view) || Validator.isEmpty(remoteUrl) || Validator.isNull(localFile)) {
            throw new IllegalArgumentException("SetImageTask: invalid argument");
        }
        this.viewRef = new WeakReference<>(view);
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
    }

    private boolean downloadIfNeeded(String remoteUrl, File localFile) {
        if (!localFile.exists()) {
            return HttpUtil.getFileFromUrl(remoteUrl, localFile);
        }
        return true;
    }

    private Bitmap decodeFromFile(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (Validator.isEmpty(remoteUrl) || Validator.isNull(localFile)) {
            return null;
        } else {
            Bitmap bitmap = null;
            if (downloadIfNeeded(remoteUrl, localFile)) {
                try {
                    bitmap = decodeFromFile(localFile);
                    if (bitmap == null) {
                        localFile.delete();
                        if (downloadIfNeeded(remoteUrl, localFile)) {
                            bitmap = decodeFromFile(localFile);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Bitmap result) {
        try {
            if (result != null) {
                View v = viewRef.get();
                if (v != null) {
                    if (v instanceof ImageView) {
                        ((ImageView) v).setImageBitmap(result);
                    } else {
                        Drawable d = new BitmapDrawable(v.getResources(), result);
                        if (Build.VERSION.SDK_INT >= 16) {
                            v.setBackground(d);
                        } else {
                            v.setBackgroundDrawable(d);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}