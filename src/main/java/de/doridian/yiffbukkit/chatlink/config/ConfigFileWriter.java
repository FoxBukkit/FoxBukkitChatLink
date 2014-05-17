package de.doridian.yiffbukkit.chatlink.config;

import de.doridian.yiffbukkit.chatlink.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigFileWriter extends FileWriter {
	public ConfigFileWriter(String file) throws IOException {
        super(new File(Main.getDataFolder(), file));
	}
}
