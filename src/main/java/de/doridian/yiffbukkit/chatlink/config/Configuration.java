package de.doridian.yiffbukkit.chatlink.config;

import java.io.BufferedReader;
import java.util.HashMap;

public class Configuration {
	private static HashMap<String,String> configValues = new HashMap<>();
	static {
		configValues.clear();
		try {
			BufferedReader stream = new BufferedReader(new ConfigFileReader("config.txt"));
			String line; int lpos;
			while((line = stream.readLine()) != null) {
				lpos = line.lastIndexOf('=');
				configValues.put(line.substring(0,lpos), line.substring(lpos+1));
			}
			stream.close();
		}
		catch (Exception e) { }
	}
	public static String getValue(String key, String def) {
		if(configValues.containsKey(key)) {
			return configValues.get(key);
		}
		return def;
	}
}
