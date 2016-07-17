package tw.kaneshih.simpletool.utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import android.util.Base64;

public final class EncUtil {
    private static byte[] genBytes(byte[] source, int offset, int size) {
        if (Validator.isNull(source)) {
            return null;
        }
        if (offset < 0) {
            return null;
        }
        if (size < 1) {
            return null;
        }
        offset %= source.length;
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = source[(offset + i) % source.length];
        }
        return bytes;
    }


    private static byte[] xor(byte[] s, byte[] k) {
        byte[] yak = new byte[s.length];
        for (int i = 0, j = 0; i < s.length; i++) {
            if (j == k.length) {
                j = 0;
            }
            yak[i] = (byte) (s[i] ^ k[j]);
            j++;
        }
        return yak;
    }

    /**
     * @param str     - non-null
     * @param encType - MD2, MD5, SHA-1, SHA-256, SHA-384 or SHA-512
     * @return
     */
    public static String encrypt(String str, String encType) {
        if (Validator.isNull(str)) {
            return null;
        }
        if (Validator.isEmpty(encType)) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(encType);
            md.update(str.getBytes());
            return toHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param in - non-null
     * @return
     */
    public static String toHexString(byte[] in) {
        if (Validator.isNull(in)) {
            return null;
        }
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < in.length; i++) {
            String hex = Integer.toHexString(0xFF & in[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * @param str - non-null
     * @return
     */
    public static String urlEncode(String str) {
        if (Validator.isNull(str)) {
            return str;
        }
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
