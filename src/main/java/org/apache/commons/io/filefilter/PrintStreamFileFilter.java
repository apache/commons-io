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
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.attribute.BasicFileAttributesPrinter;

public class PrintStreamFileFilter extends AbstractMethodFileFilter {

    public static void main(final String[] args) {
        final Path directory = args.length == 0 ? PathUtils.current() : Paths.get(args[0]);
        try {
            PathUtils.visitFileTree(PrintStreamFileFilter.systemOut(), directory);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static PrintStreamFileFilter systemOut() {
        return new PrintStreamFileFilter(System.out, FileVisitorMethod.FILE, FileVisitorMethod.PRE_DIR);
    }

    private final String dirPostExFormat = "Post %s: %s%n";
    private final String dirPostFormat = "Directory: %s%n";
    private final String dirPreAttrFormat = "Directory: %s %s%n";
    private final String dirPreFormat = "Pre %s%n";
    private final String fileAttrFormat = "%s %s%n";
    private final String fileFailFormat = "%s: %s%n";
    private final String fileFormat = "%s%n";
    private final boolean printAttributes;
    private final PrintStream printStream;

    public PrintStreamFileFilter(final PrintStream printStream, final FileVisitorMethod... fileVisitorMethod) {
        super(fileVisitorMethod);
        this.printStream = printStream;
        this.printAttributes = true;
    }

    @Override
    public boolean accept(final File file) {
        if (isCheckFile()) {
            printFile(file, null);
        }
        return true;
    }

    @Override
    protected FileVisitResult handle(final Throwable t) {
        printStream.println(t);
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        if (isCheckFile()) {
            printFile(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        if (isCheckDirPost()) {
            if (exc == null) {
                printStream.printf(dirPostFormat, dir);
            } else {
                printStream.printf(dirPostExFormat, dir, exc);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        if (isCheckDirPre()) {
            if (printAttributes(attributes)) {
                printStream.printf(dirPreFormat, dir);
            } else {
                printStream.printf(dirPreAttrFormat, dir, BasicFileAttributesPrinter.toFileString(attributes));
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean printAttributes(final BasicFileAttributes attributes) {
        return attributes == null || !printAttributes;
    }

    private void printFile(final Object file, final BasicFileAttributes attributes) {
        if (printAttributes(attributes)) {
            printStream.printf(fileFormat, file);
        } else {
            printStream.printf(fileAttrFormat, BasicFileAttributesPrinter.toFileString(attributes), file);
        }
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        if (isCheckFile()) {
            printFile(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        if (isCheckFileFailed()) {
            printStream.printf(fileFailFormat, file, exc);
        }
        return FileVisitResult.CONTINUE;
    }
}
