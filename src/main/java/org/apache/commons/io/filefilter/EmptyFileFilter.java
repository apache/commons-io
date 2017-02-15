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
import java.io.Serializable;

/**
 * This filter accepts files or directories that are empty.
 * <p>
 * If the <code>File</code> is a directory it checks that
 * it contains no files.
 * <p>
 * Example, showing how to print out a list of the
 * current directory's empty files/directories:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( EmptyFileFilter.EMPTY );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the
 * current directory's non-empty files/directories:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( EmptyFileFilter.NOT_EMPTY );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.3
 * @version $Id$
 */
public class EmptyFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 3631422087512832211L;

    /** Singleton instance of <i>empty</i> filter */
    public static final IOFileFilter EMPTY = new EmptyFileFilter();

    /** Singleton instance of <i>not-empty</i> filter */
    public static final IOFileFilter NOT_EMPTY = new NotFileFilter(EMPTY);

    /**
     * Restrictive constructor.
     */
    protected EmptyFileFilter() {
    }

    /**
     * Checks to see if the file is empty.
     *
     * @param file  the file or directory to check
     * @return {@code true} if the file or directory
     *  is <i>empty</i>, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            return files == null || files.length == 0;
        } else {
            return file.length() == 0;
        }
    }

}
