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
package com.foxelbox.foxbukkit.chatlink;

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.dependencies.threading.SimpleThreadCreator;
import com.foxelbox.foxbukkit.chatlink.commands.system.CommandSystem;
import com.foxelbox.foxbukkit.chatlink.permissions.FoxBukkitPermissionHandler;

import java.io.File;

public class Main {
	public static Configuration configuration;

	public static RedisManager redisManager;

	public static SlackHandler slackHandler;

	public static void main(String[] args) {
		configuration = new Configuration(getDataFolder());
		redisManager = new RedisManager(new SimpleThreadCreator(), configuration);

		CommandSystem.instance.scanCommands();
		FoxBukkitPermissionHandler.instance.load();

		new SlackHandler();

		new RedisHandler();

		while(true) {
			try {
				if(new File("FoxBukkitChatLink.jar.deploy").exists()) {
					System.exit(0);
					return;
				}
				Thread.sleep(5000);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static File getDataFolder() {
		return new File(".");
	}
}
