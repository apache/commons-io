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
import java.util.List;

/**
 * Filters filenames for a certain name.
 * <p>
 * For example, to print all files and directories in the 
 * current directory whose name is <code>Test</code>:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( new NameFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons IO 1.0
 * @version $Revision: 1.3 $ $Date: 2004/02/23 04:37:57 $
 * 
 * @author Henri Yandell
 * @author Stephen Colebourne
 * @author Federico Barbieri
 * @author Serge Knystautas
 * @author Peter Donald
 */
public class NameFileFilter extends AbstractFileFilter {
    
    /** The filenames to search for */
    private String[] names;

    /**
     * Constructs a new name file filter for a single name.
     * 
     * @param name  the name to allow, must not be null
     * @throws IllegalArgumentException if the prefix is null
     */
    public NameFileFilter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name must not be null");
        }
        this.names = new String[] {name};
    }

    /**
     * Constructs a new name file filter for any of an array of names.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     * 
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the names array is null
     */
    public NameFileFilter(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("The array of names must not be null");
        }
        this.names = names;
    }

    /**
     * Constructs a new name file filter for a list of names.
     * 
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the name list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public NameFileFilter(List names) {
        if (names == null) {
            throw new IllegalArgumentException("The list of names must not be null");
        }
        this.names = (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * Checks to see if the filename matches.
     * 
     * @param file  the File to check
     * @return true if the filename matches
     */
    public boolean accept(File file) {
        String name = file.getName();
        for (int i = 0; i < this.names.length; i++) {
            if (name.equals(this.names[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks to see if the filename matches.
     * 
     * @param file  the File directory
     * @param name  the filename
     * @return true if the filename matches
     */
    public boolean accept(File file, String name) {
        for (int i = 0; i < this.names.length; i++) {
            if (name.equals(this.names[i])) {
                return true;
            }
        }
        return false;
    }
    
}
