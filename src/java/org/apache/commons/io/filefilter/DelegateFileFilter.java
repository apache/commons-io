package org.apache.commons.io.filefilter;

import java.io.FilenameFilter;
import java.io.FileFilter;
import java.io.File;

/**
 */
public class DelegateFileFilter
extends AbstractFileFilter
{

    private FilenameFilter filename;
    private java.io.FileFilter file;

    public DelegateFileFilter(FilenameFilter filter) {
        if(filter == null) {
            throw new NullPointerException("Setting a filter to null will "+
                "infinitely loop. Use a NullFileFilter instead. ");
        }
        this.filename = filter;
    }

    public DelegateFileFilter(java.io.FileFilter filter) {
        if(filter == null) {
            throw new NullPointerException("Setting a filter to null will "+
                "infinitely loop. Use a NullFileFilter instead. ");
        }
        this.file = filter;
    }

    public boolean accept( File f) {
        if(file != null) {
            return file.accept(f);
        } else {
            return super.accept(f);
        }
    }

    public boolean accept( File dir, String name) {
        if(filename != null) {
            return filename.accept(dir, name);
        } else {
            return super.accept(dir, name);
        }
    }
}
