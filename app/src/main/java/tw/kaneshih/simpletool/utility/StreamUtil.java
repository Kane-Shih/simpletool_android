package tw.kaneshih.simpletool.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtil {
    private static final int BUFFER_SIZE = 4 * 1024;

    /**
     * @param is       - it'll be closed
     * @param encoding - can be null (UTF-8)
     * @return non null
     */
    public static String convertStreamToString(InputStream is, String encoding) {
        if (Validator.isEmpty(encoding)) {
            encoding = "UTF-8";
        }

        StringBuilder sb = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, encoding));

            char[] buffer = new char[BUFFER_SIZE];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
