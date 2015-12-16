package org.apache.commons.io;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

/**
 * Depth first traversal based file tree iterator that lists all the regular files in
 * under the given root directory that matches to file fileter.
 *
 * @see FileUtils#iterateFilesDepthFirst(File, IOFileFilter)
 */
public class DepthFirstFileIterator implements Iterator<File> {

    private final FileFilter fileFilter;
    private final Stack<File> stack;

    private File next;
    private int numFiles;
    private int numDirs;

    /**
     * @param root the parent directory
     */
    public DepthFirstFileIterator(File root, FileFilter fileFilter){
        this.stack = new Stack<File>();
        this.stack.add(root);
        this.next = getNext();
        this.fileFilter = fileFilter;
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public File next() {
        try {
            return next;
        } finally {
            next = getNext();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    private File getNext(){
        File[] files;
        while (!stack.isEmpty()) {
            File top = stack.pop();
            if (top == null || !top.exists() || !top.canRead()) {
                continue;
            }
            if (top.isFile()) {
                numFiles++;
                return top;
            } else {
                files = fileFilter == null ? top.listFiles() : top.listFiles(fileFilter);
                if (files != null) {
                    numDirs++;
                    Collections.addAll(stack, files);
                }
            }
        }
        return null;
    }

    /**
     * Gets number of files visited so far
     * @return number of files seen
     */
    public int getNumFiles() {
        return numFiles;
    }

    /**
     * Gets number of directories visited so far
     * @return number of directories seen
     */
    public int getNumDirs() {
        return numDirs;
    }
}
