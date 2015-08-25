package de.marius_oe.cfs.cryption;

import static de.marius_oe.cfs.configuration.Configuration.Key.Algorithm;
import static de.marius_oe.cfs.configuration.Configuration.Key.KeySize;
import static de.marius_oe.cfs.configuration.Configuration.Key.SecretKeyFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marius_oe.cfs.configuration.Configuration;

/**
 * Class to manage the keys used for decryption and encryption.
 *
 * @author Marius
 *
 */
public final class KeyManager {

	private static KeyManager instance;

	private static final Logger logger = LoggerFactory.getLogger(KeyManager.class);

	/**
	 * Returns the singleton of this class.
	 *
	 * @return singleton of this class
	 */
	public static KeyManager instance() {
		if (instance == null) {
			instance = new KeyManager();
		}
		return instance;
	}

	/** Currently loaded secret key. */
	private SecretKey secretKey;

	/**
	 * Hidden constructor.
	 */
	private KeyManager() {
	}

	private SecretKey generateKey() {
		logger.debug("Generating secret key with algorithm {} and key-size {}.", Configuration.get(Algorithm), Configuration.getInt(KeySize));
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(Configuration.get(Algorithm));
			keyGen.init(Configuration.getInt(KeySize));
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Algorithm {} is not supported.", Configuration.get(Algorithm));
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the key for de- and encryption.
	 *
	 * @return the secret key
	 */
	public SecretKey getKey() {
		if (secretKey == null) {
			loadKey();
		}
		return secretKey;
	}

	/**
	 * Loads the secret key from the key-file. If no key-file is present, a new
	 * key is generated and stored in the specified key-file.
	 */
	private void loadKey() {
		logger.debug("Loading secret key.");
		try {
			byte[] keyBytes = new byte[Configuration.getInt(KeySize) / 8];

			FileInputStream fis = new FileInputStream(Configuration.get(SecretKeyFile));
			fis.read(keyBytes);
			fis.close();

			secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, Configuration.get(Algorithm));

			logger.debug("Key has been loaded.");
		} catch (FileNotFoundException e) {
			logger.info("Key-file {} does not exists. A new key will be generated.", Configuration.get(SecretKeyFile));
			secretKey = generateKey();
			storeSecretKey();
		} catch (IOException e) {
			logger.error("Key-file {} cannot be read.", Configuration.get(SecretKeyFile));
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stores the current secretKey in the file system.
	 */
	private void storeSecretKey() {
		logger.info("Writing secretKey in file {}", Configuration.get(SecretKeyFile));
		try {
			FileOutputStream fos = new FileOutputStream(Configuration.get(SecretKeyFile), false);
			fos.write(secretKey.getEncoded());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			logger.info("Cannot write secretKey in file. Reason: {}", e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
}
