package de.marius_oe.cfs.cryption;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

public class CrypterTest {

	private static final byte[] testInput = new byte[] { 43, -97, -86, 14, -63, 112, 100, 64, -23, 0, 64, -54, -85, 68, -116, -75, -64, -76, 54, -127 };

	/**
	 * Testing the if the output of an encrypted and decrypted data stream is
	 * equals to the input data.
	 */
	@Test
	public void decryptionAndEncryptionTest() {

		boolean useCompression = false;

		// Encryption
		ByteArrayInputStream plainInputStream = new ByteArrayInputStream(testInput);
		ByteArrayOutputStream encryptedOutputStream = new ByteArrayOutputStream();

		Crypter.encrypt(plainInputStream, encryptedOutputStream, useCompression);

		// Decryption
		InputStream cryptedInputStream = new ByteArrayInputStream(encryptedOutputStream.toByteArray());
		ByteArrayOutputStream plainOutputStream = new ByteArrayOutputStream();

		Crypter.decrypt(cryptedInputStream, plainOutputStream, useCompression);

		assertArrayEquals(testInput, plainOutputStream.toByteArray());
	}

}
