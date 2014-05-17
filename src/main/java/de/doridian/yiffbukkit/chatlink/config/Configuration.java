package de.doridian.yiffbukkit.chatlink.config;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final HashMap<String,String> configValues;

    static {
        configValues = new HashMap<>();
        load();
    }

    public static void load() {
        synchronized (configValues) {
            configValues.clear();
            try {
                BufferedReader stream = new BufferedReader(new ConfigFileReader("config.txt"));
                String line;
                int lpos;
                while ((line = stream.readLine()) != null) {
                    lpos = line.lastIndexOf('=');
                    if (lpos > 0)
                        configValues.put(line.substring(0, lpos), line.substring(lpos + 1));
                }
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        synchronized (configValues) {
            try {
                PrintWriter stream = new PrintWriter(new ConfigFileWriter("config.txt"));
                for (Map.Entry<String, String> configEntry : configValues.entrySet()) {
                    stream.println(configEntry.getKey() + "=" + configEntry.getValue());
                }
                stream.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getValue(String key, String def) {
        synchronized (configValues) {
            if (configValues.containsKey(key)) {
                return configValues.get(key);
            }
            configValues.put(key, def);
        }
        save();
        return def;
    }
}
