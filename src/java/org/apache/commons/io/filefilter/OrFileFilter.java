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
 * This filter produces a logical OR of the two filters specified.
 *
 * @since Commons IO 1.0
 * @version $Revision: 1.8 $ $Date: 2004/02/23 04:37:57 $
 * 
 * @author Stephen Colebourne
 */
public class OrFileFilter extends AbstractFileFilter {
    
    /** The first filter */
    private IOFileFilter filter1;
    /** The second filter */
    private IOFileFilter filter2;

    /**
     * Constructs a new file filter that ORs the result of two other filters.
     * 
     * @param filter1  the first filter, must not be null
     * @param filter2  the second filter, must not be null
     * @throws IllegalArgumentException if either filter is null
     */
    public OrFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        if (filter1 == null || filter2 == null) {
            throw new IllegalArgumentException("The filters must not be null");
        }
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    /**
     * Checks to see if either filter is true.
     * 
     * @param file  the File to check
     * @return true if either filter is true
     */
    public boolean accept(File file) {
        return filter1.accept(file) || filter2.accept(file);
    }
    
    /**
     * Checks to see if either filter is true.
     * 
     * @param file  the File directory
     * @param name  the filename
     * @return true if either filter is true
     */
    public boolean accept(File file, String name) {
        return filter1.accept(file, name) || filter2.accept(file, name);
    }
    
}
