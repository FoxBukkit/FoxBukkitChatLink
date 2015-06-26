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

import java.util.*;

public class IgnoreList {
    private static UUIDToUUIDsRedisMap ignoredByList = new UUIDToUUIDsRedisMap("ignoredByList");
    private static UUIDToUUIDsRedisMap ignoreList = new UUIDToUUIDsRedisMap("ignoreList");

    private IgnoreList() {

    }

    public static void add(UUID ignoredPerson, UUID ignoringPerson) {
        ignoredByList.add(ignoredPerson, ignoringPerson);
        ignoreList.add(ignoringPerson, ignoredPerson);
    }

    public static void remove(UUID ignoredPerson, UUID ignoringPerson) {
        ignoredByList.remove(ignoredPerson, ignoringPerson);
        ignoreList.remove(ignoringPerson, ignoredPerson);
    }
}
