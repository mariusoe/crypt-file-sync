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
