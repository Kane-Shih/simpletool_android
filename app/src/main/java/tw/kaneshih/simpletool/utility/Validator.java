package tw.kaneshih.simpletool.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Validator {

    public static boolean isNull(int[] array) {
        return array == null;
    }

    public static boolean isNull(boolean[] array) {
        return array == null;
    }

    public static <T> boolean isNull(final T[] array) {
        return array == null;
    }

    public static boolean isNull(byte[] array) {
        return array == null;
    }

    public static <T> boolean isEmpty(final T[] array) {
        if (array == null) {
            return true;
        } else {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    return false;
                }
            }
            return true;
        }
    }

    public static <T> boolean isNull(final Collection<?> collections) {
        return collections == null;
    }

    public static <T> boolean isEmpty(final Collection<?> collections) {
        return collections == null || collections.isEmpty();
    }

    public static boolean isNull(final String str) {
        if (str == null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNull(final Object obj) {
        if (obj == null) {
            return true;
        } else {
            return false;
        }
    }
}
