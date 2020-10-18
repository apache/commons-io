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
 * This filter accepts <code>File</code>s that can be written to.
 * <p>
 * Example, showing how to print out a list of the current directory's <i>writable</i> files:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(CanWriteFileFilter.CAN_WRITE);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's <i>un-writable</i> files:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(CanWriteFileFilter.CANNOT_WRITE);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * <b>N.B.</b> For read-only files, use <code>CanReadFileFilter.READ_ONLY</code>.
 *
 * @since 1.3
 */
public class CanWriteFileFilter extends AbstractFileFilter implements Serializable {

    /** Singleton instance of <i>writable</i> filter */
    public static final IOFileFilter CAN_WRITE = new CanWriteFileFilter();

    /** Singleton instance of not <i>writable</i> filter */
    public static final IOFileFilter CANNOT_WRITE = CAN_WRITE.negate();

    private static final long serialVersionUID = 5132005214688990379L;

    /**
     * Restrictive constructor.
     */
    protected CanWriteFileFilter() {
    }

    /**
     * Checks to see if the file can be written to.
     *
     * @param file the File to check
     * @return {@code true} if the file can be written to, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        return file.canWrite();
    }

    /**
     * Checks to see if the file can be written to.
     * @param file the File to check
     *
     * @return {@code true} if the file can be written to, otherwise {@code false}.
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return toFileVisitResult(Files.isWritable(file), file);
    }

}
