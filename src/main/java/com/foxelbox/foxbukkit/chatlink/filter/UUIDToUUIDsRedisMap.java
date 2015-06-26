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

public class UUIDToUUIDsRedisMap {
    private final Map<String, String> redisMap;

    public UUIDToUUIDsRedisMap(String name) {
        redisMap = Main.redisManager.createCachedRedisMap(name);
    }

    private String[] getList(UUID ignoredPerson) {
        String list = redisMap.get(ignoredPerson.toString());
        if(list == null) {
            return new String[0];
        }
        return list.split(",");
    }

    public void add(UUID key, UUID value) {
        String[] old = getList(key);
        Set<String> newList = new HashSet<>();
        newList.addAll(Arrays.asList(old));
        newList.add(value.toString());
        setList(key, newList);
    }

    public void remove(UUID key, UUID value) {
        String[] old = getList(key);
        Set<String> newList = new HashSet<>();
        newList.addAll(Arrays.asList(old));
        newList.remove(value.toString());
        setList(key, newList);
    }

    private void setList(UUID key, Collection<String> newList) {
        if(newList.isEmpty()) {
            redisMap.remove(key.toString());
        } else {
            redisMap.put(key.toString(), Utils.concat(",", newList, 0, ""));
        }
    }
}
