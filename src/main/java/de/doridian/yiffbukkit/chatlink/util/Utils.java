/**
 * This file is part of YiffBukkitChatLink.
 *
 * YiffBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
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
