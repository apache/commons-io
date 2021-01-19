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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This filter accepts {@code File}s that can be read.
 * <p>
 * Example, showing how to print out a list of the current directory's <i>readable</i> files:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(CanReadFileFilter.CAN_READ);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's <i>un-readable</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(CanReadFileFilter.CANNOT_READ);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's <i>read-only</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(CanReadFileFilter.READ_ONLY);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * @since 1.3
 */
public class CanReadFileFilter extends AbstractFileFilter implements Serializable {

    /** Singleton instance of <i>readable</i> filter */
    public static final IOFileFilter CAN_READ = new CanReadFileFilter();

    /** Singleton instance of not <i>readable</i> filter */
    public static final IOFileFilter CANNOT_READ = CAN_READ.negate();

    /** Singleton instance of <i>read-only</i> filter */
    public static final IOFileFilter READ_ONLY = CAN_READ.and(CanWriteFileFilter.CANNOT_WRITE);

    private static final long serialVersionUID = 3179904805251622989L;

    /**
     * Restrictive constructor.
     */
    protected CanReadFileFilter() {
    }

    /**
     * Checks to see if the file can be read.
     *
     * @param file the File to check.
     * @return {@code true} if the file can be read, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        return file.canRead();
    }

    /**
     * Checks to see if the file can be read.
     * @param file the File to check.
     *
     * @return {@code true} if the file can be read, otherwise {@code false}.
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return toFileVisitResult(Files.isReadable(file), file);
    }

}
