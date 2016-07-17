package tw.kaneshih.simpletool.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class FileUtil {
    /**
     * @param file
     * @return byte array
     */
    public static byte[] convertFileToByteArray(File file) {
        if (Validator.isNull(file)) {
            return null;
        }
        byte[] byteArray = null;
        InputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        try {
            inputStream = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead;
            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return byteArray;
    }

    /**
     * @param file
     * @return file extension name
     */

    public static String getFileExtensionName(File file) {
        if (Validator.isNull(file)) {
            return null;
        }

        String name = file.getName();
        int lastDotPosition = name.lastIndexOf(".");
        if (lastDotPosition != -1) {
            return name.substring(lastDotPosition + 1);
        } else {
            return "";
        }
    }

    public static void copy(File src, File dst) throws IOException {
        if (Validator.isNull(src) || Validator.isNull(dst)) {
            return;
        }

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * @param target
     * @param encoding - can be null (UTF-8)
     * @return
     */
    public static String getStringFromFile(File target, String encoding) {
        if (Validator.isNull(target)) {
            return null;
        }
        if (Validator.isEmpty(encoding)) {
            encoding = "UTF-8";
        }

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(target);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fin == null) {
            return null;
        }

        return StreamUtil.convertStreamToString(fin, encoding);
    }

    /**
     * @param in   - will be closed after job done
     * @param file
     * @return if success
     */
    public static boolean writeInputStreamToFile(InputStream in, File file) {
        if (Validator.isNull(in)) {
            return false;
        }
        if (Validator.isNull(file)) {
            return false;
        }

        boolean result = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * implement with InputStreamReader, so works for characters.
     *
     * @param input    - it'll be finally closed
     * @param target
     * @param isAppend - whether or not to append an existing file
     * @param encoding - can be null (UTF-8)
     * @param prepend  - can be null
     * @param append   - can be null
     * @return - if success
     */
    public static boolean writeStringInputStreamToFile(InputStream input, File target, boolean isAppend,
                                                       String encoding, String prepend, String append) {
        if (Validator.isNull(input)) {
            Logcat.e("FileUtil - writeStringInputStreamToFile", "invalid input");
            return false;
        }
        if (Validator.isNull(target)) {
            Logcat.e("FileUtil - writeStringInputStreamToFile", "invalid target");
            return false;
        }
        if (Validator.isEmpty(encoding)) {
            encoding = "UTF-8";
        }

        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(input, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            return false;
        }

        boolean result = false;
        FileWriter writer = null;
        try {
            writer = new FileWriter(target, isAppend);
            if (prepend != null) {
                writer.write(prepend);
            }
            char[] buffer = new char[4096];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);
            if (append != null) {
                writer.write(append);
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * @param s
     * @param target
     * @param isAppend - whether or not to append an existing file
     */
    public static void writeToFile(String s, File target, boolean isAppend) {
        if (Validator.isNull(s)) {
            s = "null";
        }
        if (Validator.isNull(target)) {
            Logcat.e("FileUtil - writeToFile", "invalid target");
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(target, isAppend);
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] getBytesFromFile(File file) {
        if (Validator.isNull(file) || !file.exists()) {
            return null;
        }

        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            bytes = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }

    public static boolean writeBytesToFile(byte[] bytes, File file) {
        if (Validator.isNull(file)) {
            return false;
        }
        boolean isSuccess = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }
}
