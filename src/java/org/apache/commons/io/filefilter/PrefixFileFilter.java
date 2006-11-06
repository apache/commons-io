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
package org.apache.commons.io.filefilter;

import java.io.File;
import java.util.List;

/**
 * Filters filenames for a certain prefix.
 * <p>
 * For example, to print all files and directories in the 
 * current directory whose name starts with <code>Test</code>:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( new PrefixFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons IO 1.0
 * @version $Revision$ $Date$
 * 
 * @author Stephen Colebourne
 * @author Federico Barbieri
 * @author Serge Knystautas
 * @author Peter Donald
 */
public class PrefixFileFilter extends AbstractFileFilter {
    
    /** The filename prefixes to search for */
    private String[] prefixes;

    /**
     * Constructs a new Prefix file filter for a single prefix.
     * 
     * @param prefix  the prefix to allow, must not be null
     * @throws IllegalArgumentException if the prefix is null
     */
    public PrefixFileFilter(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("The prefix must not be null");
        }
        this.prefixes = new String[] {prefix};
    }

    /**
     * Constructs a new Prefix file filter for any of an array of prefixes.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     * 
     * @param prefixes  the prefixes to allow, must not be null
     * @throws IllegalArgumentException if the prefix array is null
     */
    public PrefixFileFilter(String[] prefixes) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The array of prefixes must not be null");
        }
        this.prefixes = prefixes;
    }

    /**
     * Constructs a new Prefix file filter for a list of prefixes.
     * 
     * @param prefixes  the prefixes to allow, must not be null
     * @throws IllegalArgumentException if the prefix list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public PrefixFileFilter(List prefixes) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The list of prefixes must not be null");
        }
        this.prefixes = (String[]) prefixes.toArray(new String[prefixes.size()]);
    }

    /**
     * Checks to see if the filename starts with the prefix.
     * 
     * @param file  the File to check
     * @return true if the filename starts with one of our prefixes
     */
    public boolean accept(File file) {
        String name = file.getName();
        for (int i = 0; i < this.prefixes.length; i++) {
            if (name.startsWith(this.prefixes[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks to see if the filename starts with the prefix.
     * 
     * @param file  the File directory
     * @param name  the filename
     * @return true if the filename starts with one of our prefixes
     */
    public boolean accept(File file, String name) {
        for (int i = 0; i < prefixes.length; i++) {
            if (name.startsWith(prefixes[i])) {
                return true;
            }
        }
        return false;
    }
    
}
