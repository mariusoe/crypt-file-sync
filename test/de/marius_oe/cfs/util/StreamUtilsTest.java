package de.marius_oe.cfs.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

/**
 * Tests for the {@link StreamUtils} class.
 * 
 * @author Marius
 *
 */
public class StreamUtilsTest {

	/**
	 * Testing the
	 * {@link StreamUtils#copy(java.io.InputStream, java.io.OutputStream)}
	 * method.
	 */
	@Test
	public void copyStream_01() {
		final String testString = "This is a unit test";
		byte[] inBuffer = testString.getBytes();

		ByteArrayInputStream inStream = new ByteArrayInputStream(inBuffer);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		StreamUtils.copy(inStream, outStream);

		assertEquals(testString, outStream.toString());
	}
}
