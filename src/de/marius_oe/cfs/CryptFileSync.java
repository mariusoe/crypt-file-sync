package de.marius_oe.cfs;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptFileSync {

	private static final Logger logger = LoggerFactory.getLogger(CryptFileSync.class);

	/**
	 * Main class of this project.
	 * 
	 * @param args
	 *            the program arguments
	 */
	public static void main(String[] args) {
		if (!unlimitedStrengthPolicyAvailable()) {
			logger.warn("Please install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy.");
			System.exit(0);
		}
	}

	/**
	 * Checks whether the Java Cryptography Extension (JCE) Unlimited Strength
	 * Jurisdiction Policy is installed.
	 * 
	 * @return <code>true</code> if the unlimited strength policy is installed
	 */
	public static boolean unlimitedStrengthPolicyAvailable() {
		try {
			return Cipher.getMaxAllowedKeyLength("RC5") >= 256;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
