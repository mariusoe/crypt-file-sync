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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marius_oe.cfs.util.StreamUtils;

public class Crypter {

	private static final Logger logger = LoggerFactory.getLogger(Crypter.class);

	/**
	 * Constructor.
	 */
	private Crypter() {
	}

	/**
	 * Returns the {@link Cipher} for the encryption and decryption process.
	 * 
	 * @param mode
	 *            {@link Cipher.DECRYPT_MODE} or {@link Cipher.ENCRYPT_MODE}
	 * @param iv
	 *            the initial vector which should be used in the cipher. if the
	 *            iv is <code>null</code>, a new iv will be generated.
	 * @return {@link Cipher} object
	 */
	private static Cipher getCipher(int mode, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			if (iv == null) {
				cipher.init(mode, KeyManager.instance().getKey());
			} else {
				IvParameterSpec parameterSpec = new IvParameterSpec(iv);
				cipher.init(mode, KeyManager.instance().getKey(), parameterSpec);
			}
			return cipher;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decrypts the given input stream and stores the decrypted bytes in the
	 * destinationFile. If compressStream is <code>true</code>, the given stream
	 * has been compressed and is uncompressed after decryption.
	 *
	 * @param inStream
	 *            source stream with encrypted data and iv at the beginning
	 * @param destinationStream
	 *            stream for the decrypted data
	 * @param compressStream
	 *            whether the stream was compressed before encryption
	 */
	public static void decrypt(InputStream inStream, OutputStream destinationStream, boolean compressStream) {
		logger.debug("decrypting inputstream - compressed: {}", compressStream);

		try {
			// reading iv of stream
			int ivLength = inStream.read();
			byte[] iv = new byte[ivLength];
			inStream.read(iv);

			logger.debug("Decrypt InputStream.");
			inStream = new CipherInputStream(inStream, getCipher(Cipher.DECRYPT_MODE, iv));

			if (compressStream) {
				logger.debug("Decompress InputStream.");

				try {
					inStream = new ZipInputStream(inStream);
					((ZipInputStream) inStream).getNextEntry();
				} catch (IOException e) {
					logger.debug("Error occured during unzipping - Reason: {}", e.getLocalizedMessage());
					throw new RuntimeException(e);
				}
			}

			// copy stream
			int bytesCopied = IOUtils.copy(inStream, destinationStream);

			logger.debug("decryption done. copied {} decrypted bytes to the outputstream", bytesCopied);

			inStream.close();
			destinationStream.close();
		} catch (IOException e) {
			logger.error("Decryption failed - Reason: {}", e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encrypts the given input stream and stores the encrypted data in the
	 * destinationFile.
	 *
	 * @param inStream
	 *            plain text stream
	 * @param destinationStream
	 *            stream for the encrypted data
	 * @param compressStream
	 *            whether the data should be compressed before encryption
	 */
	public static void encrypt(InputStream inStream, OutputStream destinationStream, boolean compressStream) {
		logger.debug("encrypting inputstream - compressed: {}", compressStream);

		InputStream tempInputStream;

		if (compressStream) {
			logger.debug("Compress InputStream.");
			tempInputStream = StreamUtils.zipStream(inStream);
		} else {
			tempInputStream = inStream;
		}

		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, null);

		logger.debug("Encrypt InputStream.");
		tempInputStream = new CipherInputStream(tempInputStream, cipher);

		try {
			// write iv to the beginning of the stream
			destinationStream.write((byte) cipher.getIV().length);
			destinationStream.write(cipher.getIV());

			int bytesCopied = IOUtils.copy(tempInputStream, destinationStream);

			logger.debug("encryption done. copied {} encrypted bytes to the outputstream", bytesCopied);

			tempInputStream.close();
			destinationStream.close();
		} catch (IOException e) {
			logger.error("Encryption failed - Reason: {}", e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
}
