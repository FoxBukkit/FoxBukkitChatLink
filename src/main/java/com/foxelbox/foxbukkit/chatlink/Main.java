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
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
	public static Configuration configuration;

	public static RedisManager redisManager;

	public static SlackHandler slackHandler;

	public static ZMQ.Context zmqContext;

	public static ChatQueueHandler chatQueueHandler;

	public static void main(String[] args) throws IOException {
		configuration = new Configuration(getDataFolder());
		redisManager = new RedisManager(new SimpleThreadCreator(), configuration);
		zmqContext = ZMQ.context(4);

		CommandSystem.instance.scanCommands();
		FoxBukkitPermissionHandler.instance.load();

		slackHandler = new SlackHandler(configuration);

		chatQueueHandler = new ChatQueueHandler();

		System.out.println("READY");

		Thread t = new Thread() {
			public void run() {
				while (true) {
					try {
						if (new File("FoxBukkitChatLink.jar.deploy").exists()) {
							ZeroMQConfigurator.shutdown();
							System.exit(0);
							return;
						}
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(!"exit".equalsIgnoreCase(br.readLine())) {
			try {
				Thread.sleep(200);
			} catch (Exception e) { }
		}
		ZeroMQConfigurator.shutdown();
		System.exit(0);
	}

	public static File getDataFolder() {
		return new File(".");
	}
}
