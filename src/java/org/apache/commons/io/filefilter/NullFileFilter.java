package org.apache.commons.io.filefilter;

import java.io.File;

/**
 * Filters nothing.
 */
public class NullFileFilter
extends AbstractFileFilter
{

    public boolean accept( File f ) {
        return true;
    }

    public boolean accept( File dir, String name ) {
        return true;
    }
}
