package org.apache.commons.io.filefilter;

import java.io.FilenameFilter;
import java.io.File;

/**
 * An abstract class which brings the FileFilter and FilenameFilter 
 * interfaces together, through the IO.FileFilter interface. Note that 
 * you <b>must</b> override one of the methods ellse your class will 
 * infinitely loop.
 */
public abstract class AbstractFileFilter
implements FileFilter
{

    /** Defined in FileFilter */
    public boolean accept( File f) {
        return accept( f.getParentFile(), f.getName());
    }

    /** Defined in FilenameFilter */
    public boolean accept( File dir, String name) {
        return accept( new File( dir.getName() + name ) );
    }
}
