package org.apache.commons.io.filefilter;

import java.io.FilenameFilter;
import java.io.File;

/**
 * An interface which brings the FileFilter and FilenameFilter 
 * interfaces together.
 */
public interface FileFilter
extends java.io.FileFilter, FilenameFilter
{

    /** Defined in java.io.FileFilter */
    public boolean accept( File f);

    /** Defined in FilenameFilter */
    public boolean accept( File dir, String name);
}
