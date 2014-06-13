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
package com.foxelbox.foxbukkit.chatlink.permissions;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class FoxBukkitPermissionHandler {
	public static final FoxBukkitPermissionHandler instance = new FoxBukkitPermissionHandler();

	private boolean loaded = false;
	private final Map<String,String> playerGroups = Main.redisManager.createCachedRedisMap("playergroups");
	private final HashMap<String,HashSet<String>> groupPermissions = new HashMap<>();
	private final HashMap<String,HashSet<String>> groupProhibitions = new HashMap<>();

	public void load() {
		if(loaded) return;
		reload();
	}

	public void reload() {
		loaded = true;
		groupPermissions.clear();
		groupProhibitions.clear();

        final File file = new File(Main.getDataFolder(), "permissions.txt");

        try {
            String currentGroup = null;
            HashSet<String> currentPermissions = null;
            HashSet<String> currentProhibitions = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.length() < 1) continue;
                    char c = line.charAt(0);
                    if (c == '-') {
                        line = line.substring(1).trim();
                        currentPermissions.remove(line);
                        currentProhibitions.add(line);
                    } else if (c == '+') {
                        line = line.substring(1).trim();
                        currentPermissions.add(line);
                        currentProhibitions.remove(line);
                    } else {
                        if (currentGroup != null) {
                            groupPermissions.put(currentGroup, currentPermissions);
                            groupProhibitions.put(currentGroup, currentProhibitions);
                        }
                        int i = line.indexOf(' ');
                        currentPermissions = new HashSet<>();
                        currentProhibitions = new HashSet<>();
                        if (i > 0) {
                            currentGroup = line.substring(0, i).trim();
                            String tmp = line.substring(i + 1).trim();
                            currentPermissions.addAll(groupPermissions.get(tmp));
                            currentProhibitions.addAll(groupProhibitions.get(tmp));
                        } else {
                            currentGroup = line;
                        }
                    }
                }
                if (currentGroup != null) {
                    groupPermissions.put(currentGroup, currentPermissions);
                    groupProhibitions.put(currentGroup, currentProhibitions);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
	}

	public void save() {

	}


	public boolean has(Player player, String permission) {
		return has(player.uuid, permission);
	}

	public boolean has(UUID uuid, String permission) {
		permission = permission.toLowerCase();

		String currentGroup = getGroup(uuid);

		HashSet<String> currentPermissions = groupPermissions.get(getGroup(uuid));
        if(currentPermissions == null) return false;

		if(currentPermissions.contains(permission)) return true;

		HashSet<String> currentProhibitions = groupProhibitions.get(currentGroup);
		if(currentProhibitions != null && currentProhibitions.contains(permission)) return false;

		int xpos = 0;
		String tperm = permission;
		while((xpos = tperm.lastIndexOf('.')) > 0) {
			tperm = tperm.substring(0, xpos);
			String tperm2 = tperm + ".*";
			if(currentProhibitions != null && currentProhibitions.contains(tperm2)) { currentProhibitions.add(permission); return false; }
			if(currentPermissions.contains(tperm2)) { currentPermissions.add(permission); return true; }
		}

		if(currentProhibitions != null && currentProhibitions.contains("*")) { currentProhibitions.add(permission); return false; }
		if(currentPermissions.contains("*")) { currentPermissions.add(permission); return true; }

		if(currentProhibitions == null) {
			currentProhibitions = new HashSet<>();
			groupProhibitions.put(currentGroup, currentProhibitions);
		}
		currentProhibitions.add(permission);
		return false;
	}

	public String getGroup(UUID uuid) {
		String result = playerGroups.get(uuid.toString());
		if(result == null)
			return "guest";
		return result;
	}

	public void setGroup(UUID uuid, String group) {
		group = group.toLowerCase();
		playerGroups.put(uuid.toString(), group);
		save();
	}

	public boolean inGroup(UUID uuid, String group) {
		return getGroup(uuid).equalsIgnoreCase(group);
	}
}