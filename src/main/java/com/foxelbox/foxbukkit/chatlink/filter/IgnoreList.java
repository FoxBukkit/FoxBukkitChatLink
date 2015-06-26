/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.chatlink.filter;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

import java.util.*;

public class IgnoreList {
    private static Map<String, String> ignoreList = Main.redisManager.createCachedRedisMap("ignoreList");

    private IgnoreList() {

    }

    private static String[] getList(UUID uuid) {
        String list = ignoreList.get(uuid.toString());
        if(list == null) {
            return new String[0];
        }
        return list.split(",");
    }

    public static void add(UUID uuid, UUID other) {
        String[] old = getList(uuid);
        Set<String> newList = new HashSet<>();
        newList.addAll(Arrays.asList(old));
        newList.add(other.toString());
        setList(uuid, newList);
    }

    public static void remove(UUID uuid, UUID other) {
        String[] old = getList(uuid);
        Set<String> newList = new HashSet<>();
        newList.addAll(Arrays.asList(old));
        newList.remove(other.toString());
        setList(uuid, newList);
    }

    private static void setList(UUID uuid, Collection<String> newList) {
        ignoreList.put(uuid.toString(), Utils.concat(",", newList, 0, ""));
    }
}
