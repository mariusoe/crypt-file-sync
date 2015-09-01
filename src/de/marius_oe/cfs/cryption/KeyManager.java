/**
 * The MIT License
 * Copyright (c) 2015 Marius Oehler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
