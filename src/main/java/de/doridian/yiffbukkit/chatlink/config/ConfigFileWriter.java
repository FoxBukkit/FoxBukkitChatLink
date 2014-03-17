package de.doridian.yiffbukkit.chatlink.config;

import java.io.FileWriter;
import java.io.IOException;

public class ConfigFileWriter extends FileWriter {
	public ConfigFileWriter(String file) throws IOException {
		super("./" + file);
	}
}
