package de.doridian.yiffbukkit.chatlink.util;

import java.util.Collection;

public class Utils {
    public static String concat(String separator, Collection<String> parts, int start, String defaultText) {
        // TODO: optimize
        return concatArray(separator, parts.toArray(new String[parts.size()]), start, defaultText);
    }

    public static String concatArray(String separator, String[] array, int start, String defaultText) {
        if (array.length <= start)
            return defaultText;

        if (array.length <= start + 1)
            return array[start]; // optimization

        StringBuilder ret = new StringBuilder(array[start]);
        for(int i = start + 1; i < array.length; i++) {
            ret.append(separator);
            ret.append(array[i]);
        }
        return ret.toString();
    }
}
