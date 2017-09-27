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
 * A file filter that always returns true.
 *
 * @since 1.0
 * @version $Id$
 * @see FileFilterUtils#trueFileFilter()
 */
public class TrueFileFilter implements IOFileFilter, Serializable {

    private static final long serialVersionUID = 8782512160909720199L;
    /**
     * Singleton instance of true filter.
     * @since 1.3
     */
    public static final IOFileFilter TRUE = new TrueFileFilter();
    /**
     * Singleton instance of true filter.
     * Please use the identical TrueFileFilter.TRUE constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = TRUE;

    /**
     * Restrictive constructor.
     */
    protected TrueFileFilter() {
    }

    /**
     * Returns true.
     *
     * @param file  the file to check (ignored)
     * @return true
     */
    @Override
    public boolean accept(final File file) {
        return true;
    }

    /**
     * Returns true.
     *
     * @param dir  the directory to check (ignored)
     * @param name  the filename (ignored)
     * @return true
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return true;
    }

}
