package de.marius_oe.cfs.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for watching the file system for changes.
 * 
 * @author Marius
 *
 */
public final class FileWatcher implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

	private final Map<WatchKey, Path> keyMap;
	private final WatchService watcher;
	private boolean isProcessing = true;
	private Thread executingThread;

	public FileWatcher() {
		try {
			keyMap = new HashMap<>();
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Register the given path to the watcher and notify if any files or
	 * directories are modified.
	 * 
	 * @param path
	 *            path to observe
	 * @throws IOException
	 */
	public void register(Path path) throws IOException {
		logger.debug("Register path {}", path);
		WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keyMap.put(key, path);
	}

	/**
	 * Register the given path and all sub directories to the watcher and notify
	 * if any files or directories are modified.
	 * 
	 * @param path
	 *            directory to observe
	 * @throws IOException
	 */
	public void registerAll(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
				register(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Override
	public void run() {
		executingThread = Thread.currentThread();
		processEvents();
	}

	/**
	 * Stops the file watcher
	 */
	public void stopWatching() {
		logger.debug("Stopping file watcher");
		isProcessing = false;
		if (executingThread != null) {
			executingThread.interrupt();
		}
	}

	/**
	 * Processes the WatchEvents.
	 */
	@SuppressWarnings("unchecked")
	public void processEvents() {
		logger.debug("Start processing events");
		while (isProcessing) {

			// Wait for a key
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				return;
			}

			// Get the path related to the WatchKey
			Path currentPath = keyMap.get(key);
			if (currentPath == null) {
				logger.debug("No Path for received WatchKey available.");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				// Skip Overflow Events
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// Get the file that triggered this event
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path file = ev.context();

				if (kind == ENTRY_CREATE) {
					logger.debug("Created {} {}", currentPath, file);
					// TODO register children
				}

				// TODO notify
				logger.debug("Notify {} {}", currentPath, file);
			}

			// Reset the key -- this step is critical if you want to
			// receive further watch events. If the key is no longer valid,
			// the directory is inaccessible so exit the loop.
			boolean valid = key.reset();
			if (!valid) {
				logger.error("Key reset was not sucessfull.");
				break;
			}

		}
	}
}
