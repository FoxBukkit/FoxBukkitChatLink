package de.doridian.yiffbukkit.chatlink;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedisManager {
	static final JedisPool readJedisPool = createPool("arcticfox-mc.doridian.de");
	private static final JedisPool[] writeJedisPools = new JedisPool[] { readJedisPool };

	private static final String REDIS_PASSWORD = "SECRET";
	private static final int REDIS_DB = 3;

	private static JedisPool createPool(String host) {
		return new JedisPool(new JedisPoolConfig(), host, 6379, 20000, REDIS_PASSWORD, REDIS_DB);
	}

	public static class RedisMap implements Map<String, String> {
		private final String name;
		public RedisMap(String name) {
			this.name = name;
		}

		@Override
		public int size() {
			Jedis jedis = readJedisPool.getResource();

			int ret;
			if(!jedis.exists(name))
				ret = 0;
			else
				ret = (int)(long)jedis.hlen(name);

			readJedisPool.returnResource(jedis);

			return ret;
		}

		@Override
		public boolean isEmpty() {
			return (size() <= 0);
		}

		@Override
		public boolean containsKey(Object key) {
			Jedis jedis = readJedisPool.getResource();
			boolean exists = jedis.hexists(name, key.toString());
			readJedisPool.returnResource(jedis);
			return exists;
		}

		@Override
		public boolean containsValue(Object value) {
			return values().contains(value.toString());
		}

		@Override
		public String get(Object key) {
			Jedis jedis = readJedisPool.getResource();
			String ret = jedis.hget(name, key.toString());
			readJedisPool.returnResource(jedis);
			return ret;
		}

		@Override
		public Set<String> keySet() {
			Jedis jedis = readJedisPool.getResource();
			Set<String> keys = jedis.hkeys(name);
			readJedisPool.returnResource(jedis);
			if(keys == null)
				return new HashSet<String>();
			return keys;
		}

		@Override
		public Collection<String> values() {
			Jedis jedis = readJedisPool.getResource();
			Collection<String> values = jedis.hvals(name);
			readJedisPool.returnResource(jedis);
			if(values == null)
				return new ArrayList<String>();
			return values;
		}

		@Override
		public Set<Entry<String, String>> entrySet() {
			Jedis jedis = readJedisPool.getResource();
			Map<String, String> entryMap = jedis.hgetAll(name);
			readJedisPool.returnResource(jedis);
			if(entryMap == null)
				return new HashSet<Entry<String, String>>();
			return entryMap.entrySet();
		}

		@Override
		public String put(String key, String value) {
			String old = get(key);
			for(JedisPool writeJedisPool : writeJedisPools) {
				Jedis jedis = writeJedisPool.getResource();
				jedis.hset(name, key, value);
				writeJedisPool.returnResource(jedis);
			}
			return old;
		}

		@Override
		public String remove(Object key) {
			String old = get(key);
			String keyS = key.toString();
			for(JedisPool writeJedisPool : writeJedisPools) {
				Jedis jedis = writeJedisPool.getResource();
				jedis.hdel(name, keyS);
				writeJedisPool.returnResource(jedis);
			}
			return old;
		}

		@Override
		public void putAll(Map<? extends String, ? extends String> m) {
			for(JedisPool writeJedisPool : writeJedisPools) {
				Jedis jedis = writeJedisPool.getResource();
				for(Entry<? extends String, ? extends String> e : m.entrySet()) {
					jedis.hset(name, e.getKey(),  e.getValue());
				}
				writeJedisPool.returnResource(jedis);
			}
		}

		@Override
		public void clear() {
			for(JedisPool writeJedisPool : writeJedisPools) {
				Jedis jedis = writeJedisPool.getResource();
				jedis.del(name);
				writeJedisPool.returnResource(jedis);
			}
		}
	}

	public static Map<String,String> createKeptMap(String name) {
		return new RedisMap(name);
	}
}
