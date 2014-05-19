package de.doridian.yiffbukkit.chatlink;

import de.doridian.dependencies.config.Configuration;
import de.doridian.dependencies.redis.RedisManager;

import java.io.File;

public class Main {
    public static Configuration configuration;

    public static RedisManager redisManager;

	public static void main(String[] args) {
        configuration = new Configuration(getDataFolder());
        redisManager = new RedisManager(configuration);

		new RedisHandler();

		while(true) {
			try {
				if(new File("YiffBukkitChatLink.jar.deploy").exists()) {
					System.exit(0);
					return;
				}
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public static File getDataFolder() {
        return new File(".");
    }
}
