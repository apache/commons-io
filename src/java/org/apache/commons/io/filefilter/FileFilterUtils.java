/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io.filefilter;

import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Useful utilities for working with file filters. It provides access to all
 * file filter implementations in this package so you don't have to import
 * every classes you use.
 * 
 * @since Commons IO 1.0
 * @version $Revision: 1.6 $ $Date: 2003/11/22 20:03:52 $
 * 
 * @author Henri Yandell
 * @author Stephen Colebourne
 * @author Jeremias Maerki
 */
public class FileFilterUtils {
    
    /**
     * FileFilterUtils is not normally instantiated.
     */
    public FileFilterUtils() {
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a filter that returns true if the filename starts with the specified text.
     * 
     * @param prefix  the filename prefix
     * @return a prefix checking filter
     */
    public static IOFileFilter prefixFileFilter(String prefix) {
        return new PrefixFileFilter(prefix);
    }

    /**
     * Returns a filter that returns true if the filename ends with the specified text.
     * 
     * @param suffix  the filename suffix
     * @return a suffix checking filter
     */
    public static IOFileFilter suffixFileFilter(String suffix) {
        return new SuffixFileFilter(suffix);
    }

    /**
     * Returns a filter that returns true if the filename matches the specified text.
     * 
     * @param name  the filename
     * @return a name checking filter
     */
    public static IOFileFilter nameFileFilter(String name) {
        return new NameFileFilter(name);
    }

    /**
     * Returns a filter that checks if the file is a directory.
     * 
     * @return directory file filter
     */
    public static IOFileFilter directoryFileFilter() {
        return DirectoryFileFilter.INSTANCE;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Returns a filter that ANDs the two specified filters.
     * 
     * @param filter1  the first filter
     * @param filter2  the second filter
     * @return a filter that ANDs the two specified filters
     */
    public static IOFileFilter andFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new AndFileFilter(filter1, filter2);
    }

    /**
     * Returns a filter that ORs the two specified filters.
     * 
     * @param filter1  the first filter
     * @param filter2  the second filter
     * @return a filter that ORs the two specified filters
     */
    public static IOFileFilter orFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new OrFileFilter(filter1, filter2);
    }

    /**
     * Returns a filter that NOTs the specified filter.
     * 
     * @param filter  the filter to invert
     * @return a filter that NOTs the specified filter
     */
    public static IOFileFilter notFileFilter(IOFileFilter filter) {
        return new NotFileFilter(filter);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a filter that always returns true.
     * 
     * @return a true filter
     */
    public static IOFileFilter trueFileFilter() {
        return TrueFileFilter.INSTANCE;
    }

    /**
     * Returns a filter that always returns false.
     * 
     * @return a false filter
     */
    public static IOFileFilter falseFileFilter() {
        return FalseFileFilter.INSTANCE;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Returns an <code>IOFileFilter</code> that wraps the
     * <code>FileFilter</code> instance.
     * 
     * @param filter  the filter to be wrapped
     * @return a new filter that implements IOFileFilter
     */
    public static IOFileFilter asFileFilter(FileFilter filter) {
        return new DelegateFileFilter(filter);
    }

    /**
     * Returns an <code>IOFileFilter</code> that wraps the
     * <code>FilenameFilter</code> instance.
     * 
     * @param filter  the filter to be wrapped
     * @return a new filter that implements IOFileFilter
     */
    public static IOFileFilter asFileFilter(FilenameFilter filter) {
        return new DelegateFileFilter(filter);
    }

    //-----------------------------------------------------------------------

    /* Constructed on demand and then cached */
    private static IOFileFilter cvsFilter = null;

    /**
     * Resturns an IOFileFilter that ignores CVS directories. You may optionally
     * pass in an existing IOFileFilter in which case it is extended to exclude
     * CVS directories.
     * @param filter IOFileFilter to modify, null if a new IOFileFilter
     * should be created
     * @return the requested (combined) filter
     */
    public static IOFileFilter makeCVSAware(IOFileFilter filter) {
        if (cvsFilter == null) {
            cvsFilter = andFileFilter(directoryFileFilter(), 
                notFileFilter(nameFileFilter("CVS")));
        }
        if (filter == null) {
            return cvsFilter;
        } else {
            return andFileFilter(filter, cvsFilter);
        }
    }

}
