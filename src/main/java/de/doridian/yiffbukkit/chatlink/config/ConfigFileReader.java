package de.doridian.yiffbukkit.chatlink.config;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ConfigFileReader extends FileReader {
	public ConfigFileReader(String file) throws FileNotFoundException {
		super("./" + file);
	}
}
