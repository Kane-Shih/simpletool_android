package tw.kaneshih.simpletool.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * @param in           - we won't close it
     * @param requiredSize - width or height pixel
     * @return
     */
    public static Bitmap decodeInputStream(InputStream in, int requiredSize) {
        if (Validator.isNull(in)) {
            return null;
        }

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, option);

        int scale = 1;
        while (option.outWidth / scale / 2 >= requiredSize && option.outHeight / scale / 2 >= requiredSize) {
            scale *= 2;
        }
        Logcat.d(TAG, "scale" + scale);
        Logcat.d(TAG, "option.outWidth" + option.outWidth);
        Logcat.d(TAG, "option.outHeight" + option.outHeight);
        option.inJustDecodeBounds = false;
        option.inSampleSize = scale;
        return BitmapFactory.decodeStream(in, null, option);
    }

    /**
     * @param bmp
     * @param format  - JPG, PNG, ...etc.
     * @param quality - 0-100 for JPG
     * @param target
     */
    public static void saveBitmapToFile(Bitmap bmp, Bitmap.CompressFormat format, int quality, File target) {
        if (Validator.isNull(bmp) || Validator.isNull(format) || Validator.isNull(target)) {
            return;
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(target);
            bmp.compress(format, quality, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable ignore) {
            }
        }
    }

    public static ByteBuffer getByteBufferFromBitmap(Bitmap bmp) {
        if (Validator.isNull(bmp)) {
            return null;
        }

        int bytes = bmp.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bmp.copyPixelsToBuffer(buffer);
        return buffer;
    }

    public static float getRotationDegrees(File file) {
        if (Validator.isNull(file)) {
            return 0;
        }
        float degrees = 0;
        String extName = FileUtil.getFileExtensionName(file);
        if (extName.equalsIgnoreCase("jpg") || extName.equalsIgnoreCase("jpeg")) {
            try {
                int orientation = new ExifInterface(file.getAbsolutePath()).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    degrees = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    degrees = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    degrees = 270;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return degrees;
    }

    public static void setRotationDegrees(File file, float degrees) throws IOException {
        if (Validator.isNull(file)) {
            return;
        }

        String extName = FileUtil.getFileExtensionName(file);
        if (extName.equalsIgnoreCase("jpg") || extName.equalsIgnoreCase("jpeg")) {
            int value = ExifInterface.ORIENTATION_NORMAL;
            switch ((int) degrees) {
                case 90:
                    value = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 180:
                    value = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 270:
                    value = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                case 0:
                default:
                    break;
            }
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(value));
            exif.saveAttributes();
        }
    }

    public static Bitmap createNewRotatedBitmap(float degrees, Bitmap sourceBitmap) {
        if (Validator.isNull(sourceBitmap)) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                sourceBitmap,
                0,
                0,
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                matrix,
                true);
    }

    /**
     * @param originalFile
     * @param fileSizeLimitInBytes - include
     * @param degrees
     * @param newFile
     */
    public static void saveResizedBitmapFile(File originalFile, long fileSizeLimitInBytes, float degrees, File newFile) {
        if (Validator.isNull(originalFile) || Validator.isNull(newFile)) {
            return;
        }

        if (!originalFile.exists()) {
            throw new IllegalArgumentException("originalFile is null or doesn't exist");
        }
        Logcat.d(TAG, "originalFile.length(): " + originalFile.length());
        Logcat.d(TAG, "fileSizeLimitInBytes: " + fileSizeLimitInBytes);
        Logcat.d(TAG, "degrees: " + degrees);
        Logcat.d(TAG, "newFile: " + newFile.getAbsolutePath());

        boolean isExceedSize = (originalFile.length() >= fileSizeLimitInBytes);
        if (!isExceedSize) {
            try {
                FileUtil.copy(originalFile, newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (degrees != 0) {
                try {
                    setRotationDegrees(newFile, degrees);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Logcat.d(TAG, "RESULT " + (degrees != 0 ? "(rotate + save)" : "(save only)"));
            return;
        }

        InputStream in = null;
        Bitmap bitmap = null;
        try {
            in = new FileInputStream(originalFile);
            bitmap = BitmapFactory.decodeStream(in, null, null);
            in.close();
            Logcat.d(TAG, "bmp size : " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // rotate if needed
            if (degrees != 0) {
                Bitmap newBitmap = createNewRotatedBitmap(degrees, bitmap);
                bitmap.recycle();
                bitmap = newBitmap;
            }

            // compress
            saveBitmapToFile(bitmap, Bitmap.CompressFormat.JPEG, 50, newFile);

            // scale again if needed
            if (newFile.length() >= fileSizeLimitInBytes) {
                Logcat.d(TAG, "save to quality 50%, still too large, make it smaller => to 1/4");
                Bitmap newBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        bitmap.getWidth() / 2,
                        bitmap.getHeight() / 2,
                        true);
                bitmap.recycle();
                bitmap = newBitmap;

                saveBitmapToFile(bitmap, Bitmap.CompressFormat.JPEG, 50, newFile);

                int quality = 40;
                while (newFile.length() >= fileSizeLimitInBytes) {
                    newFile.delete();
                    Logcat.d(TAG, "compress to " + quality);
                    saveBitmapToFile(bitmap, Bitmap.CompressFormat.JPEG, quality, newFile);
                    if (quality > 10) {
                        quality /= 2;
                    } else if (quality == 0) {
                        break;
                    } else {
                        quality = 0;
                    }
                }
                Logcat.d(TAG, "RESULT (compress + scale) " + newFile.length());
            } else {
                Logcat.d(TAG, "RESULT (compress only) " + newFile.length());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int r = 0;
        if (width > height) {
            r = height;
        } else {
            r = width;
        }
        Bitmap backgroundBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(backgroundBmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, r, r);
        canvas.drawRoundRect(rect, r / 2, r / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return backgroundBmp;
    }

    public static Bitmap toRoundBitmapWithRing(Bitmap bitmap, int ringColor, int ringThickness) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int squareWidth = Math.min(w, h);
        int radius = squareWidth / 2;
        Bitmap output = Bitmap.createBitmap(
                squareWidth + ringThickness * 2,
                squareWidth + ringThickness * 2,
                Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle(radius + ringThickness, radius + ringThickness, radius, p);

        p.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        Rect srcRect = new Rect();
        if (w >= h) {
            srcRect.top = 0;
            srcRect.left = (w - h) / 2;
            srcRect.right = srcRect.left + h;
            srcRect.bottom = h;
        } else {
            srcRect.top = (h - w) / 2;
            srcRect.left = 0;
            srcRect.right = w;
            srcRect.bottom = srcRect.top + w;
        }

        c.drawBitmap(bitmap, srcRect, new Rect(ringThickness, ringThickness, squareWidth + ringThickness, squareWidth
                + ringThickness), p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ringColor);
        p.setStrokeWidth(ringThickness);
        c.drawCircle(radius + ringThickness, radius + ringThickness, radius, p);
        return output;
    }
}