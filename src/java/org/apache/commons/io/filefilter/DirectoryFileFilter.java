/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.filefilter;

import java.io.File;

/**
 * This filter accepts <code>File</code>s that are directories.
 * <p>
 * For example, here is how to print out a list of the 
 * current directory's subdirectories:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( DirectoryFileFilter.INSTANCE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons IO 1.0
 * @version $Revision: 1.7 $ $Date: 2004/02/23 04:37:57 $
 * 
 * @author Henri Yandell
 * @author Stephen Colebourne
 * @author Peter Donald
 */
public class DirectoryFileFilter extends AbstractFileFilter {
    
    /** Singleton instance of directory filter */
    public static final IOFileFilter INSTANCE = new DirectoryFileFilter();
    
    /**
     * Restrictive consructor.
     */
    protected DirectoryFileFilter() {
    }
    
    /**
     * Checks to see if the file is a directory.
     * 
     * @param file  the File to check
     * @return true if the file is a directory
     */
    public boolean accept(File file) {
        return file.isDirectory();
    }
    
}
