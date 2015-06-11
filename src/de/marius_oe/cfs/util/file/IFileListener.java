package de.marius_oe.cfs.util.file;

import java.nio.file.Path;

public interface IFileListener {

	/**
	 * Will be called when the specified file was modified.
	 * 
	 * @param file
	 *            file that was modified
	 */
	void onModify(Path file);

	/**
	 * Will be called when the specified file was deleted.
	 * 
	 * @param file
	 *            file that was deleted
	 */
	void onDelete(Path file);

	/**
	 * Will be called when the specified file was created.
	 * 
	 * @param file
	 *            file that was created
	 */
	void onCreate(Path file);
}
