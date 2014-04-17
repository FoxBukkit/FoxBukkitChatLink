package de.doridian.yiffbukkit.chatlink;

import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RedisHandler extends AbstractJedisPubSub {
	public RedisHandler() {
		RedisManager.readJedisPool.getResource().subscribe(this, "yiffbukkit:from_server");
	}

	@Override
	public void onMessage(final String channel, final String c_message) {
		try {
			//SERVER|USER|MESSAGE
			final String[] split = c_message.split("\\|", 4);
			final String name = PlayerHelper.getFullPlayerName(UUID.fromString(split[1]), split[2]);
			String message = split[3];
			if(message.length() > 4 && message.substring(0, 4).toLowerCase().equals("/me ")) {
				message = "\u00a7f* " + name + "\u00a77 " + message.substring(4);
			} else if(message.equals("\u0123join")) {
				message = "\u00a72[+] \u00a7e" + name + "\u00a7e joined!";
			} else if(message.equals("\u0123quit")) {
				message = "\u00a74[-] \u00a7e" + name + "\u00a7e disconnected!";
			} else if(message.length() > 6 && message.substring(0, 6).equals("\u0123kick ")) {
				message = "\u00a74[-] \u00a7e" + name + "\u00a7e was kicked (" + message.substring(6) + ")!";
			} else {
				message = name + "\u00a7f: " + message;
			}
			final Jedis jedis = RedisManager.readJedisPool.getResource();
			//SERVER|USER|MESSAGE
			jedis.publish("yiffbukkit:to_server", split[0]+"|"+split[1]+"|"+split[2]+"|"+message);
			RedisManager.readJedisPool.returnResource(jedis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
