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
 * This filter accepts <code>File</code>s that are hidden.
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>hidden</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( HiddenFileFilter.HIDDEN );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>visible</i> (i.e. not hidden) files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( HiddenFileFilter.VISIBLE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.3
 * @version $Id$
 */
public class HiddenFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 8930842316112759062L;

    /** Singleton instance of <i>hidden</i> filter */
    public static final IOFileFilter HIDDEN  = new HiddenFileFilter();

    /** Singleton instance of <i>visible</i> filter */
    public static final IOFileFilter VISIBLE = new NotFileFilter(HIDDEN);

    /**
     * Restrictive constructor.
     */
    protected HiddenFileFilter() {
    }

    /**
     * Checks to see if the file is hidden.
     *
     * @param file  the File to check
     * @return {@code true} if the file is
     *  <i>hidden</i>, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        return file.isHidden();
    }

}
