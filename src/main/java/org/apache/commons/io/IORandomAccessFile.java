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

package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * Extends {@link RandomAccessFile} to provide access to the {@link File} and {@code mode} passed on construction.
 *
 * @since 2.18.0
 * @see RandomAccessFile
 * @see RandomAccessFileMode
 */
public final class IORandomAccessFile extends RandomAccessFile {

    private final File file;
    private final String mode;

    /**
     * Constructs a new instance by calling {@link RandomAccessFile#RandomAccessFile(File, String)}.
     *
     * @param file the file object
     * @param mode the access mode, as described in {@link RandomAccessFile#RandomAccessFile(File, String)}.
     * @throws FileNotFoundException Thrown by {@link RandomAccessFile#RandomAccessFile(File, String)}.
     * @see RandomAccessFile#RandomAccessFile(File, String)
     */
    public IORandomAccessFile(final File file, final String mode) throws FileNotFoundException {
        super(file, mode);
        this.file = file;
        this.mode = mode;
    }

    /**
     * Constructs a new instance by calling {@link RandomAccessFile#RandomAccessFile(String, String)}.
     *
     * @param name the file object
     * @param mode the access mode, as described in {@link RandomAccessFile#RandomAccessFile(String, String)}.
     * @throws FileNotFoundException Thrown by {@link RandomAccessFile#RandomAccessFile(String, String)}.
     * @see RandomAccessFile#RandomAccessFile(String, String)
     */
    public IORandomAccessFile(final String name, final String mode) throws FileNotFoundException {
        super(name, mode);
        this.file = name != null ? new File(name) : null;
        this.mode = mode;
    }

    /**
     * Gets the file passed to {@link #IORandomAccessFile(File, String)}.
     *
     * @return the file passed to {@link #IORandomAccessFile(File, String)}.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the mode passed to {@link #IORandomAccessFile(File, String)}.
     *
     * @return the mode passed to {@link #IORandomAccessFile(File, String)}.
     */
    public String getMode() {
        return mode;
    }

    /**
     * Returns the pathname string of this abstract pathname. This is just the string returned by the {@link File#toString()} method.
     *
     * @return The string form of the File's abstract pathname.
     * @see File#toString()
     */
    @Override
    public String toString() {
        return Objects.toString(file);
    }

}
