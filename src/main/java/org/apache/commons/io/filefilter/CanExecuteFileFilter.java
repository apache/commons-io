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
 * This filter accepts <code>File</code>s that can be executed.
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>executable</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( CanExecuteFileFilter.CAN_EXECUTE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>un-executable</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( CanExecuteFileFilter.CANNOT_EXECUTE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 2.7
 *
 */
public class CanExecuteFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 3179904805251622989L;

    /** Singleton instance of <i>executable</i> filter */
    public static final IOFileFilter CAN_EXECUTE = new CanExecuteFileFilter();

    /** Singleton instance of not <i>executable</i> filter */
    public static final IOFileFilter CANNOT_EXECUTE = new NotFileFilter(CAN_EXECUTE);

    /**
     * Restrictive constructor.
     */
    protected CanExecuteFileFilter() {
    }

    /**
     * Checks to see if the file can be executed.
     *
     * @param file  the File to check.
     * @return {@code true} if the file can be executed, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        return file.canExecute();
    }

}
