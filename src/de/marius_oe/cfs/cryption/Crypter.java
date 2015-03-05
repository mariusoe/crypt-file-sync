package de.marius_oe.cfs.cryption;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marius_oe.cfs.util.StreamUtils;

public class Crypter {

	private static final Logger logger = LoggerFactory.getLogger(Crypter.class);

	/**
	 * Returns the {@link Cipher} for the encryption and decryption process.
	 *
	 * @param mode
	 *            {@link Cipher.DECRYPT_MODE} or {@link Cipher.ENCRYPT_MODE}
	 * @return {@link Cipher} object
	 */
	private static Cipher getCipher(int mode) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			if (KeyManager.instance().getIV() == null) {
				cipher.init(mode, KeyManager.instance().getKey());
				KeyManager.instance().storeIV(cipher.getIV());
			} else {
				cipher.init(mode, KeyManager.instance().getKey(), KeyManager.instance().getIV());
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
	 *            source stream with encrypted data
	 * @param destinationFile
	 *            destination file of decrypted data
	 * @param compressStream
	 *            whether the stream was compressed before encryption
	 */
	public void decrypt(InputStream inStream, File destinationFile, boolean compressStream) {
		logger.debug("Decrypt InputStream - Destination file: {} - Is compress: {}", destinationFile.getAbsoluteFile(), compressStream);

		logger.debug("Decrypt InputStream.");
		inStream = new CipherInputStream(inStream, getCipher(Cipher.DECRYPT_MODE));

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

		try {
			BufferedOutputStream foStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
			StreamUtils.copy(inStream, foStream, false);
			foStream.close();
			logger.debug("Decryption done - Data stored in file: {}", destinationFile.getAbsoluteFile());
		} catch (IOException e) {
			logger.error("Cannot store decrypted data in file {} - Reason: {}", destinationFile.getAbsoluteFile(), e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encrypts the given input stream and stores the encrypted data in the
	 * destinationFile.
	 *
	 * @param inStream
	 *            plain text stream
	 * @param destinationFile
	 *            destination file for the encrypted data
	 * @param compressStream
	 *            whether the data should be compressed before encryption
	 */
	public void encrypt(InputStream inStream, File destinationFile, boolean compressStream) {
		logger.debug("Encrypt InputStream - Destination file: {} - Compress: {}", destinationFile.getAbsoluteFile(), compressStream);

		if (compressStream) {
			logger.debug("Compress InputStream.");
			inStream = StreamUtils.zipStream(inStream);
		}

		logger.debug("Encrypt InputStream.");
		inStream = new CipherInputStream(inStream, getCipher(Cipher.ENCRYPT_MODE));

		try {
			BufferedOutputStream foStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
			StreamUtils.copy(inStream, foStream, false);
			foStream.close();
			logger.debug("Encryption done - Data stored in file: {}", destinationFile.getAbsoluteFile());
		} catch (IOException e) {
			logger.error("Cannot store encrypted data in file {} - Reason: {}", destinationFile.getAbsoluteFile(), e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
}
