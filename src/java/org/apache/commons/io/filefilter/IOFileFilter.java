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
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An interface which brings the FileFilter and FilenameFilter 
 * interfaces together.
 * 
 * @since Commons IO 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/23 04:37:57 $
 * 
 * @author Henri Yandell
 * @author Stephen Colebourne
 */
public interface IOFileFilter extends FileFilter, FilenameFilter {

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FileFilter}.
     * 
     * @param file  the File to check
     * @return true if this file matches the test
     */
    public boolean accept(File file);

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FilenameFilter}.
     * 
     * @param dir  the directory File to check
     * @param name  the filename within the directory to check
     * @return true if this file matches the test
     */
    public boolean accept(File dir, String name);
    
}
