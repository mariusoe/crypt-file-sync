package de.marius_oe.cfs.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Marius
 *
 */
public final class Configuration implements Serializable {

	/**	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Enumeration which represent the confiuration entries.
	 */
	public enum Key {
		Algorithm("algorithm"), KeySize("key_size"), IvFile("initial_vector_file"), SecretKeyFile("key_file"), SyncFolders("sync_folders");

		/*
		 * The key that is used in the config-file
		 */
		private String key;

		/*
		 * Hidden constructor.
		 */
		private Key(String key) {
			this.key = key;
		}

		/**
		 * Returns the key that is used in the config-file.
		 * 
		 * @return Key for this config entry.
		 */
		public String getKey() {
			return key;
		}
	}

	private static final String defaultConfigurationFile = "res/config.properties";

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	/**
	 * The loaded configuration.
	 */
	private static Properties properties;

	private static void load() {
		logger.debug("Loading configuration file..");
		properties = new Properties();
		try {
			properties.load(new FileInputStream(defaultConfigurationFile));
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	/**
	 * Returns a configuration value.
	 * 
	 * @param key
	 *            The key of the desired configuration entry
	 * @return the value of the configuration entry
	 */
	public static String get(Key key) {
		if (properties == null) {
			load();
		}
		return properties.getProperty(key.getKey());
	}

	/**
	 * Returns a configuration value.
	 * 
	 * @param key
	 *            The key of the desired configuration entry
	 * @return the value of the configuration entry
	 */
	public static Integer getInt(Key key) {
		if (properties == null) {
			load();
		}
		return Integer.parseInt(properties.getProperty(key.getKey()));
	}

	/**
	 * Returns a {@link Path} array containing all folders that have to be
	 * synchronized.
	 * 
	 * @return {@link Path} array containing folders to synchronize
	 */
	public static Path[] getSynchronizedFolders() {
		String[] syncFolders = get(Key.SyncFolders).split(";");
		return Arrays.stream(syncFolders).map(folder -> Paths.get(folder)).toArray(length -> new Path[length]);
	}
}
