package de.doridian.yiffbukkit.chatlink.config;

import de.doridian.yiffbukkit.chatlink.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ConfigFileReader extends FileReader {
	public ConfigFileReader(String file) throws FileNotFoundException {
		super(new File(Main.getDataFolder(), file));
	}
}
