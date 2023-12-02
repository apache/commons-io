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
 * This filter accepts {@link File}s that are symbolic links.
 * <p>
 * For example, here is how to print out a list of the real files
 * within the current directory:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = FileUtils.current();
 * String[] files = dir.list(SymbolicLinkFileFilter.INSTANCE);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = PathUtils.current();
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(SymbolicLinkFileFilter.INSTANCE);
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
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 2.11.0
 * @see FileFilterUtils#fileFileFilter()
 */
public class SymbolicLinkFileFilter extends AbstractFileFilter implements Serializable {
    /*
     * Note to developers: The unit test needs to create symbolic links to files. However, on
     * Windows, this can't be done without admin privileges. This class is designed to allow a
     * unit test to works around this by doing two things: 1) This separates the class logic from
     * the call to identify a symbolic link, and 2) It allows the unit test to override that
     * symbolic link call on Windows only.
     * This means we can write unit tests that will run on all machines. On Windows, the unit test
     * can't create a symbolic link without admin privileges, so the unit tests won't
     * completely test all the necessary behavior on Windows, but they will still test the class
     * logic. Be careful not to break this, but be aware of it when writing unit tests. You can
     * still maintain this class and its unit tests on Windows. The one method that won't get
     * tested on Windows is not likely to change, and will be tested properly when it gets run
     * on Apache servers.
     */

    /**
     * Singleton instance of file filter.
     */
    public static final SymbolicLinkFileFilter INSTANCE = new SymbolicLinkFileFilter();

    private static final long serialVersionUID = 1L;

    /**
     * Restrictive constructor.
     */
    protected SymbolicLinkFileFilter() {
    }

    /**
     * Constructs a new instance.
     *
     * @param onAccept What to do on acceptance.
     * @param onReject What to do on rejection.
     * @since 2.12.0.
     */
    public SymbolicLinkFileFilter(final FileVisitResult onAccept, final FileVisitResult onReject) {
        super(onAccept, onReject);
    }

    /**
     * Checks to see if the file is a symbolic link.
     *
     * @param file  the File to check
     * @return true if the file exists and is a symbolic link to either another file or a directory,
     *         false otherwise.
     */
    @Override
    public boolean accept(final File file) {
        return isSymbolicLink(file.toPath());
    }

    /**
     * Checks to see if the file is a symbolic link.
     *
     * @param path the File Path to check
     * @return {@code onAccept} from {@link #SymbolicLinkFileFilter(FileVisitResult, FileVisitResult)} if the file exists and is a symbolic link to either
     *         another file or a directory; returns {@code onReject} otherwise.
     */
    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return toFileVisitResult(isSymbolicLink(path));
    }

    /**
     * Delegates to {@link Files#isSymbolicLink(Path)} for testing.
     * <p>
     * Using package access for unit tests. To facilitate unit testing, all calls to test if the file is a symbolic should go through this method. (See the unit
     * test for why.)
     * </p>
     *
     * @param filePath The filePath to test
     * @return true if the file exists and is a symbolic link to either a file or directory, false otherwise.
     */
    boolean isSymbolicLink(final Path filePath) {
        return Files.isSymbolicLink(filePath);
    }
}
