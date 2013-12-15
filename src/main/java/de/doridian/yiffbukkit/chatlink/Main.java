package de.doridian.yiffbukkit.chatlink;

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
	}
}
