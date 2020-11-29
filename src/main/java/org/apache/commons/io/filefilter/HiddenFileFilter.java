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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This filter accepts <code>File</code>s that are hidden.
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>hidden</i> files:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(HiddenFileFilter.HIDDEN);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the
 * current directory's <i>visible</i> (i.e. not hidden) files:
 * </p>
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(HiddenFileFilter.VISIBLE);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = Paths.get("");
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(HiddenFileFilter.HIDDEN);
 * //
 * // Walk one dir
 * Files.<b>walkFileTree</b>(dir, Collections.emptySet(), 1, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getFileList());
 * //
 * visitor.getPathCounters().reset();
 * //
 * // Walk dir tree
 * Files.<b>walkFileTree</b>(dir, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getDirList());
 * System.out.println(visitor.getFileList());
 * </pre>
 *
 * @since 1.3
 */
public class HiddenFileFilter extends AbstractFileFilter implements Serializable {

    /** Singleton instance of <i>hidden</i> filter */
    public static final IOFileFilter HIDDEN  = new HiddenFileFilter();

    private static final long serialVersionUID = 8930842316112759062L;

    /** Singleton instance of <i>visible</i> filter */
    public static final IOFileFilter VISIBLE = HIDDEN.negate();

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

    /**
     * Checks to see if the file is hidden.
     * @param file  the File to check
     *
     * @return {@code true} if the file is
     *  <i>hidden</i>, otherwise {@code false}.
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        try {
            return toFileVisitResult(Files.isHidden(file), file);
        } catch (final IOException e) {
            return handle(e);
        }
    }

}
