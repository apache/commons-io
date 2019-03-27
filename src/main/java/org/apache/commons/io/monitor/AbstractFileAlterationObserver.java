/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.monitor;

import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOCase;

public abstract class AbstractFileAlterationObserver<F, D>
		implements IFileAlterationObserver<F, D> {
	
	/**
	 * An empty array of type <code>File</code>.
	 */
	public static final IFile[] EMPTY_FILE_ARRAY = new IFile[0];
	
	protected final List<FileAlterationListener<F, D>> listeners = new CopyOnWriteArrayList<>();
	
	protected FileEntry rootEntry;
	protected FileFilter fileFilter;
	protected Comparator<IFile> comparator;
	
	public AbstractFileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter,
		IOCase caseSensitivity){
		if (rootEntry == null) {
			throw new IllegalArgumentException("Root entry is missing");
		}
		if (rootEntry.getFile() == null) {
			throw new IllegalArgumentException("Root directory is missing");
		}
		
		this.rootEntry = rootEntry;
		this.fileFilter = fileFilter;
		
		if (caseSensitivity == null || caseSensitivity.equals(IOCase.SYSTEM)) {
			this.comparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;
		} else if (caseSensitivity.equals(IOCase.INSENSITIVE)) {
			this.comparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
		} else {
			this.comparator = NameFileComparator.NAME_COMPARATOR;
		}
	}
	
	/**
	 * Add a file system listener.
	 *
	 * @param listener
	 *            The file system listener
	 */
	public void addListener(final FileAlterationListener<F, D> listener){
		if (listener != null) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a file system listener.
	 *
	 * @param listener
	 *            The file system listener
	 */
	public void removeListener(final FileAlterationListener<F, D> listener){
		if (listener != null) {
			while (listeners.remove(listener)) {}
		}
	}
	
	/**
	 * Returns the set of registered file system listeners.
	 *
	 * @return The file system listeners
	 */
	public Iterable<FileAlterationListener<F, D>> getListeners(){
		return listeners;
	}
	
	/**
	 * Return the fileFilter.
	 *
	 * @return the fileFilter
	 * @since 2.1
	 */
	public FileFilter getFileFilter(){
		return fileFilter;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 */
	protected abstract F unwrapFile(IFile file);
	
	protected abstract D unwrapDirectory(IFile file);
	
	/**
	 * Fire directory/file change events to the registered listeners.
	 *
	 * @param entry
	 *            The previous file system entry
	 * @param file
	 *            The current file
	 */
	private void doMatch(final FileEntry entry, final IFile file){
		if (entry.refresh(file)) {
			for (final FileAlterationListener<F, D> listener : listeners) {
				if (entry.isDirectory()) {
					listener.onDirectoryChange(unwrapDirectory(file));
				} else {
					listener.onFileChange(unwrapFile(file));
				}
			}
		}
	}
	
	/**
	 * Fire directory/file delete events to the registered listeners.
	 *
	 * @param entry
	 *            The file entry
	 */
	private void doDelete(final FileEntry entry){
		for (final FileAlterationListener<F, D> listener : listeners) {
			if (entry.isDirectory()) {
				listener.onDirectoryDelete(unwrapDirectory(entry.getFile()));
			} else {
				listener.onFileDelete(unwrapFile(entry.getFile()));
			}
		}
	}
	
	/**
	 * Fire directory/file created events to the registered listeners.
	 *
	 * @param entry
	 *            The file entry
	 */
	private void doCreate(final FileEntry entry){
		for (final FileAlterationListener<F, D> listener : listeners) {
			if (entry.isDirectory()) {
				listener.onDirectoryCreate(unwrapDirectory(entry.getFile()));
			} else {
				listener.onFileCreate(unwrapFile(entry.getFile()));
			}
		}
		final FileEntry[] children = entry.getChildren();
		for (final FileEntry aChildren : children) {
			doCreate(aChildren);
		}
	}
	
	/**
	 * Create a new file entry for the specified file.
	 *
	 * @param parent
	 *            The parent file entry
	 * @param file
	 *            The file to create an entry for
	 * @return A new file entry
	 */
	private FileEntry createFileEntry(final FileEntry parent, final IFile file){
		final FileEntry entry = parent.newChildInstance(file);
		entry.refresh(file);
		final FileEntry[] children = doListFiles(file, entry);
		entry.setChildren(children);
		return entry;
	}
	
	/**
	 * List the files
	 * 
	 * @param file
	 *            The file to list files for
	 * @param entry
	 *            the parent entry
	 * @return The child files
	 */
	protected FileEntry[] doListFiles(final IFile file, final FileEntry entry){
		final IFile[] files = listFiles(file);
		final FileEntry[] children =
			files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
		for (int i = 0; i < files.length; i++) {
			children[i] = createFileEntry(entry, files[i]);
		}
		return children;
	}
	
	/**
	 * List the contents of a directory
	 *
	 * @param file
	 *            The file to list the contents of
	 * @return the directory contents or a zero length array if the empty or the file is not a
	 *         directory
	 */
	protected IFile[] listFiles(final IFile file){
		IFile[] children = null;
		if (file.isDirectory()) {
			children = fileFilter == null ? file.listFiles() : file.listFiles(fileFilter);
		}
		if (children == null) {
			children = EMPTY_FILE_ARRAY;
		}
		if (comparator != null && children.length > 1) {
			Arrays.sort(children, comparator);
		}
		return children;
	}
	
	/**
	 * Compare two file lists for files which have been created, modified or deleted.
	 *
	 * @param parent
	 *            The parent entry
	 * @param previous
	 *            The original list of files
	 * @param files
	 *            The current list of files
	 */
	protected void checkAndNotify(final FileEntry parent, final FileEntry[] previous,
		final IFile[] files){
		int c = 0;
		final FileEntry[] current =
			files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
		for (final FileEntry entry : previous) {
			while (c < files.length && comparator.compare(entry.getFile(), files[c]) > 0) {
				current[c] = createFileEntry(parent, files[c]);
				doCreate(current[c]);
				c++;
			}
			if (c < files.length && comparator.compare(entry.getFile(), files[c]) == 0) {
				doMatch(entry, files[c]);
				checkAndNotify(entry, entry.getChildren(), listFiles(files[c]));
				current[c] = entry;
				c++;
			} else {
				checkAndNotify(entry, entry.getChildren(), EMPTY_FILE_ARRAY);
				doDelete(entry);
			}
		}
		for (; c < files.length; c++) {
			current[c] = createFileEntry(parent, files[c]);
			doCreate(current[c]);
		}
		parent.setChildren(current);
	}
	
}
