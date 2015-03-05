package de.marius_oe.cfs.cryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

	private static final String ALGORITHM = "AES";

	private static KeyManager instance;
	private static final String IV_FILE = "iv.file";
	private static final String KEY_FILE = "secret.key";
	private static final int KEY_SIZE = 256;

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

	/** Currently loaded initialization vector. */
	private IvParameterSpec initializationVector;

	/** Currently loaded secret key. */
	private SecretKey secretKey;

	/**
	 * Hidden constructor.
	 */
	private KeyManager() {
	}

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
	 * Returns the stored initialization Vector.
	 *
	 * @return byte[] containing the IV
	 */
	public IvParameterSpec getIV() {
		if (initializationVector == null) {
			loadIV();
		}
		return initializationVector;
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
	 * Load the initialization vector from file.
	 */
	private void loadIV() {
		logger.debug("Loading initialization vector.");
		try {
			byte[] tempArray = new byte[1024];

			FileInputStream fis = new FileInputStream(IV_FILE);
			int bytesRead = fis.read(tempArray);
			fis.close();

			initializationVector = new IvParameterSpec(Arrays.copyOf(tempArray, bytesRead));

			logger.debug("IV has been loaded.");
		} catch (FileNotFoundException e) {
			logger.info("IV-file {} does not exists.", IV_FILE);
		} catch (IOException e) {
			logger.error("IV-file {} cannot be read.", IV_FILE);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads the secret key from the key-file. If no key-file is present, a new
	 * key is generated and stored in the specified key-file.
	 */
	private void loadKey() {
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
	 * Stores the given initialization vector.
	 *
	 * @param iv
	 *            initialization vector to store
	 */
	public void storeIV(byte[] iv) {
		initializationVector = new IvParameterSpec(iv);

		logger.info("Writing initializationVector in file {}", IV_FILE);
		try {
			FileOutputStream fos = new FileOutputStream(IV_FILE, false);
			fos.write(initializationVector.getIV());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			logger.info("Cannot write initializationVector in file. Reason: {}", e.getLocalizedMessage());
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
}
