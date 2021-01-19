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

package org.apache.commons.io.file.attribute;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.apache.commons.io.file.PathUtils;

/**
 * Prints {@link BasicFileAttributes} allowing for custom formats.
 * 
 * @since 2.9.0
 */
public class BasicFileAttributesPrinter {

    /**
     * Prints the {@link BasicFileAttributes} for the given {@link Path} strings.
     * 
     * @param args {@link Path} strings.
     */
    public static void main(String[] args) {
        for (String arg : args) {
            try {
                final Path path = Paths.get(arg);
                System.out.print(path);
                System.out.print(": ");
                System.out.println(toString(readBasicFileAttributes(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static BasicFileAttributes readBasicFileAttributes(final Path path) throws IOException {
        return PathUtils.readBasicFileAttributes(path);
    }

    private static final String FORMAT_FULL = "D%s O%s F%s S%s %s %s %s %,9d %s";
    private static final String FORMAT_FILE = "S%s %s %s %s %,9d %s";
    private static final String FORMAT_DIR = "S%s %s %s %s %,9d %s";
    private static final String EMPTY = "";

    private static char toSign(final boolean value) {
        return toSign(value, '+', '-');
    }

    private static char toSign(final boolean value, char trueChar, char falseChar) {
        return value ? trueChar : falseChar;
    }

    public static String toDirString(final BasicFileAttributes attributes) {
        if (attributes == null) {
            return EMPTY;
        }
        // Convert FileTime to instant for constant-width formatting.
        // @formatter:off
        return String.format(FORMAT_DIR,
            toSign(attributes.isSymbolicLink()),
            attributes.creationTime().toInstant(),
            attributes.lastAccessTime().toInstant(),
            attributes.lastModifiedTime().toInstant(),
            attributes.size(),
            Objects.toString(attributes.fileKey(), EMPTY)
        );
        // @formatter:on
    }

    public static String toFileString(final BasicFileAttributes attributes) {
        if (attributes == null) {
            return EMPTY;
        }
        // Convert FileTime to instant for constant-width formatting.
        // @formatter:off
        return String.format(FORMAT_FILE,
            toSign(attributes.isSymbolicLink()),
            attributes.creationTime().toInstant(),
            attributes.lastAccessTime().toInstant(),
            attributes.lastModifiedTime().toInstant(),
            attributes.size(),
            Objects.toString(attributes.fileKey(), EMPTY)
        );
        // @formatter:on
    }

    public static String toString(final BasicFileAttributes attributes) {
        if (attributes == null) {
            return EMPTY;
        }
        // Convert FileTime to instant for constant-width formatting.
        // @formatter:off
        return String.format(FORMAT_FULL,
            toSign(attributes.isDirectory()),
            toSign(attributes.isOther()),
            toSign(attributes.isRegularFile()),
            toSign(attributes.isSymbolicLink()),
            attributes.creationTime().toInstant(),
            attributes.lastAccessTime().toInstant(),
            attributes.lastModifiedTime().toInstant(),
            attributes.size(),
            Objects.toString(attributes.fileKey(), EMPTY)
        );
        // @formatter:on
    }

}
