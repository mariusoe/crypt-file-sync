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
package de.marius_oe.cfs.util.file;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private Thread executingThread;
	private boolean isProcessing = true;
	private final Map<WatchKey, Path> keyMap;
	private final WatchService watcher;
	private List<IFileListener> fileListener;

	/**
	 * Constructor.
	 */
	public FileWatcher() {
		try {
			keyMap = new HashMap<>();
			watcher = FileSystems.getDefault().newWatchService();
			fileListener = new ArrayList<>();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers the given {@link IFileListener} to this {@link FileWatcher}.
	 * 
	 * @param listener
	 *            listener that will be registered
	 * @return true if the listener was succsesfully added
	 */
	public boolean registerFileListener(IFileListener listener) {
		logger.debug("Registering fileListener");
		return fileListener.add(listener);
	}

	/**
	 * Unregisters the given {@link IFileListener}.
	 * 
	 * @param listener
	 *            listener that will be unregistered
	 * @return true if the listener was succsefully removed
	 */
	public boolean unregisterFileListener(IFileListener listener) {
		logger.debug("Unregister fileListener");
		return fileListener.remove(listener);
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

				// Notify all listeners
				if (kind == ENTRY_CREATE) {
					logger.debug("Created {} {} - Exists: {}", currentPath, file, file.toFile().exists());
					fileListener.stream().forEach(listener -> listener.onCreate(file));
				} else if (kind == ENTRY_DELETE) {
					logger.debug("Delete {} {}", currentPath, file);
					fileListener.stream().forEach(listener -> listener.onDelete(file));
				} else if (kind == ENTRY_MODIFY) {
					logger.debug("Modified {} {}", currentPath, file);
					fileListener.stream().forEach(listener -> listener.onModify(file));
				} else {
					logger.debug("Event: {} - {} {}", kind.name(), currentPath, file);
				}
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
	 * Stops the file watcher.
	 */
	public void stopWatching() {
		logger.debug("Stopping file watcher");
		isProcessing = false;
		if (executingThread != null) {
			executingThread.interrupt();
		}
	}
}
