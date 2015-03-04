package de.marius_oe.cfs.cryption;

import java.io.File;
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

/**
 * Class to manage the keys used for decryption and encryption.
 * 
 * @author Marius
 *
 */
public final class KeyManager {

	private static final Logger logger = LoggerFactory.getLogger(KeyManager.class);

	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 256;
	private static final String KEY_FILE = "secret.key";

	private static KeyManager instance;

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

	/**
	 * Hidden constructor.
	 */
	private KeyManager() {
	}

	/** Currently loaded secret key. */
	private SecretKey secretKey;

	private SecretKey generateKey() {
		logger.debug("Generating secret key with algorithm {} and key-size {}.", ALGORITHM, KEY_SIZE);
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
			keyGen.init(KEY_SIZE);
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Algorithm {} is not supported.", ALGORITHM);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads the secret key from the key-file. If no key-file is present, a new
	 * key is generated and stored in the specified key-file.
	 */
	public void loadKey() {
		logger.debug("Loading secret key.");
		try {
			byte[] keyBytes = new byte[KEY_SIZE / 8];

			FileInputStream fis = new FileInputStream(KEY_FILE);
			fis.read(keyBytes);
			fis.close();

			secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, ALGORITHM);
			
			logger.debug("Key has been loaded.");
		} catch (FileNotFoundException e) {
			logger.info("Key-file {} does not exists. A new key will be generated.", KEY_FILE);
			secretKey = generateKey();
			storeSecretKey();
		} catch (IOException e) {
			logger.error("Key-file {} cannot be read.", KEY_FILE);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stores the current secretKey in the file system.
	 */
	private void storeSecretKey() {
		logger.info("Writing secretKey in file {}", KEY_FILE);
		try {
			FileOutputStream fos = new FileOutputStream(KEY_FILE, false);
			fos.write(secretKey.getEncoded());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			logger.info("Cannot write secretKey in file. Reason: {}", e.getLocalizedMessage());
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
}
