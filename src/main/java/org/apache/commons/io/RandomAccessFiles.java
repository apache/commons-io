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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Objects;

import org.apache.commons.io.channels.FileChannels;

/**
 * Works with {@link RandomAccessFile}.
 *
 * @since 2.13.0
 */
public class RandomAccessFiles {

    /**
     * Tests if two RandomAccessFile contents are equal.
     *
     * @param raf1 A RandomAccessFile.
     * @param raf2 Another RandomAccessFile.
     * @return true if the contents of both RandomAccessFiles are equal, false otherwise.
     * @throws IOException if an I/O error occurs.
     * @since 2.15.0
     */
    @SuppressWarnings("resource") // See comments
    public static boolean contentEquals(final RandomAccessFile raf1, final RandomAccessFile raf2) throws IOException {
        // Short-circuit test
        if (Objects.equals(raf1, raf2)) {
            return true;
        }
        // Short-circuit test
        final long length1 = length(raf1);
        final long length2 = length(raf2);
        if (length1 != length2) {
            return false;
        }
        if (length1 == 0 && length2 == 0) {
            return true;
        }
        // Dig in and to the work
        // We do not close FileChannels because that closes the owning RandomAccessFile.
        // Instead, the caller is assumed to manage the given RandomAccessFile objects.
        final FileChannel channel1 = raf1.getChannel();
        final FileChannel channel2 = raf2.getChannel();
        return FileChannels.contentEquals(channel1, channel2, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    private static long length(final RandomAccessFile raf) throws IOException {
        return raf != null ? raf.length() : 0;
    }

    /**
     * Reads a byte array starting at "position" for "length" bytes.
     *
     * @param input    The source RandomAccessFile.
     * @param position The offset position, measured in bytes from the beginning of the file, at which to set the file pointer.
     * @param length   How many bytes to read.
     * @return a new byte array.
     * @throws IOException If the first byte cannot be read for any reason other than end of file, or if the random access file has been closed, or if some
     *                     other I/O error occurs.
     */
    public static byte[] read(final RandomAccessFile input, final long position, final int length) throws IOException {
        input.seek(position);
        return IOUtils.toByteArray(input::read, length);
    }

    /**
     * Resets the given file to position 0.
     *
     * @param raf The RandomAccessFile to reset.
     * @return The given RandomAccessFile.
     * @throws IOException If {@code pos} is less than {@code 0} or if an I/O error occurs.
     * @since 2.15.0
     */
    public static RandomAccessFile reset(final RandomAccessFile raf) throws IOException {
        raf.seek(0);
        return raf;
    }

    /**
     * Make private in 3.0.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public RandomAccessFiles() {
        // empty
    }
}
