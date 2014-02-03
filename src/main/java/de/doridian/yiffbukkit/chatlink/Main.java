package de.doridian.yiffbukkit.chatlink;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		new Thread() {
			public void run() {
				new RedisHandler();
			}
		}.start();
		new Thread() {
			public void run() {
				new XmlRedisHandler();
			}
		}.start();

		new Thread() {
			public void run() {
				while(true) {
					try {
						if(new File("YiffBukkitChatLink.jar.deploy").exists()) {
							RedisManager.readJedisPool.destroy();
							System.exit(0);
							return;
						}
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
