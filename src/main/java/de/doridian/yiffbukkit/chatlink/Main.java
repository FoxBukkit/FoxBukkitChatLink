package de.doridian.yiffbukkit.chatlink;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		RedisManager.initialize();
		Thread t;
		t = new Thread() {
			public void run() {
				new RedisHandler();
			}
		};
		t.setDaemon(true);
		t.start();
		t = new Thread() {
			public void run() {
				new XmlRedisHandler();
			}
		};
		t.setDaemon(true);
		t.start();

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
