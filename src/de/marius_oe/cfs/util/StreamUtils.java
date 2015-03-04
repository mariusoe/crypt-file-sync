package de.marius_oe.cfs.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author Marius
 *
 */
public class StreamUtils {

	private static ExecutorService threadPool;

	public static ExecutorService getThreadPool() {
		if (threadPool == null) {
			threadPool = Executors.newCachedThreadPool();
		}
		return threadPool;
	}

	/**
	 * Copies the input stream into the output stream. The method blocks until
	 * the copy process is done.
	 * 
	 * @param inStream
	 *            the source stream
	 * @param outStream
	 *            the destination stream
	 */
	public static void copy(InputStream inStream, OutputStream outStream) {
		copy(inStream, outStream, false);
	}

	/**
	 * Copies the input stream into the output stream.
	 * 
	 * @param inStream
	 *            the source stream
	 * @param outStream
	 *            the destination stream
	 * @param doParallel
	 *            Defines whether the copy process runs in a own thread
	 */
	public static void copy(InputStream inStream, OutputStream outStream, boolean doParallel) {
		CopyWorker worker = new CopyWorker();
		worker.inStream = inStream;
		worker.outStream = outStream;

		if (doParallel) {
			getThreadPool().execute(worker);
		} else {
			worker.run();
		}
	}

	/**
	 * Takes the given input stream and returns an @ InputStream} with the
	 * compressed data.
	 * 
	 * @param inStream
	 *            stream to compress
	 * @return @ InputStream} with the compressed data
	 */
	public static InputStream zipStream(InputStream inStream) {
		try {
			PipedOutputStream outPipe = new PipedOutputStream();
			PipedInputStream destinationStream = new PipedInputStream(outPipe);

			ZipWorker worker = new ZipWorker();
			worker.inStream = inStream;
			worker.outStream = outPipe;

			getThreadPool().execute(worker);

			return destinationStream;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes writes the input stream into the output stream.
	 * 
	 * @param inStream
	 *            the source stream
	 * @param outStream
	 *            the destination stream
	 */
	private static void copyStream(InputStream inStream, OutputStream outStream) {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Runnable class to copy a stream into an other.
	 */
	private static class CopyWorker implements Runnable {

		InputStream inStream;
		OutputStream outStream;

		@Override
		public void run() {
			copyStream(inStream, outStream);
		}
	}

	/**
	 * Runnable class to copy a stream into an other.
	 */
	private static class ZipWorker implements Runnable {

		InputStream inStream;
		OutputStream outStream;

		@Override
		public void run() {
			try {
				ZipOutputStream zipStream = new ZipOutputStream(outStream);
				zipStream.setLevel(Deflater.BEST_COMPRESSION);
				zipStream.putNextEntry(new ZipEntry("entry"));

				copyStream(inStream, zipStream);

				zipStream.closeEntry();
				zipStream.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
