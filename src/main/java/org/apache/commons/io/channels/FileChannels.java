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

package org.apache.commons.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

/**
 * Works with {@link FileChannel}.
 *
 * @since 2.15.0
 */
public final class FileChannels {

    /**
     * Tests if two file channel contents are equal starting at their respective current positions.
     *
     * @param channel1       A file channel.
     * @param channel2       Another file channel.
     * @param bufferCapacity The two internal buffer capacities, in bytes.
     * @return true if the contents of both RandomAccessFiles are equal, false otherwise.
     * @throws IOException if an I/O error occurs.
     * @deprecated Use {@link #contentEquals(SeekableByteChannel, SeekableByteChannel, int)}.
     */
    @Deprecated
    public static boolean contentEquals(final FileChannel channel1, final FileChannel channel2, final int bufferCapacity) throws IOException {
        return contentEquals((SeekableByteChannel) channel1, channel2, bufferCapacity);
    }

    /**
     * Tests if two readable byte channel contents are equal starting at their respective current positions.
     *
     * @param channel1       A readable byte channel.
     * @param channel2       Another readable byte channel.
     * @param bufferCapacity The two internal buffer capacities, in bytes.
     * @return true if the contents of both RandomAccessFiles are equal, false otherwise.
     * @throws IOException if an I/O error occurs or the timeout is met.
     * @since 2.19.0
     */
    public static boolean contentEquals(final ReadableByteChannel channel1, final ReadableByteChannel channel2, final int bufferCapacity) throws IOException {
        // Before making any changes, please test with org.apache.commons.io.jmh.IOUtilsContentEqualsInputStreamsBenchmark
        if (bufferCapacity <= 0) {
            throw new IllegalArgumentException();
        }
        // Short-circuit test
        if (Objects.equals(channel1, channel2)) {
            return true;
        }
        // Dig in and do the work
        final ByteBuffer c1Buffer = ByteBuffer.allocateDirect(bufferCapacity);
        final ByteBuffer c2Buffer = ByteBuffer.allocateDirect(bufferCapacity);
        int c1ReadNum = -1;
        int c2ReadNum = -1;
        boolean c1Read = true;
        boolean c2Read = true;
        boolean equals = false;
        // If a channel is a non-blocking channel, it may return 0 bytes read for any given call.
        while (true) {
            // don't call compact() in this method to avoid copying
            if (c1Read) {
                c1ReadNum = channel1.read(c1Buffer);
                c1Buffer.position(0);
                c1Read = c1ReadNum >= 0;
            }
            if (c2Read) {
                c2ReadNum = channel2.read(c2Buffer);
                c2Buffer.position(0);
                c2Read = c2ReadNum >= 0;
            }
            if (c1ReadNum == IOUtils.EOF && c2ReadNum == IOUtils.EOF) {
                return equals || c1Buffer.equals(c2Buffer);
            }
            if (c1ReadNum == 0 || c2ReadNum == 0) {
                Thread.yield();
            }
            if (c1ReadNum == 0 && c2ReadNum == IOUtils.EOF || c2ReadNum == 0 && c1ReadNum == IOUtils.EOF) {
                continue;
            }
            if (c1ReadNum != c2ReadNum) {
                final int limit = Math.min(c1ReadNum, c2ReadNum);
                if (limit == IOUtils.EOF) {
                    return false;
                }
                c1Buffer.limit(limit);
                c2Buffer.limit(limit);
                if (!c1Buffer.equals(c2Buffer)) {
                    return false;
                }
                equals = true;
                c1Buffer.limit(bufferCapacity);
                c2Buffer.limit(bufferCapacity);
                c1Read = c2ReadNum > c1ReadNum;
                c2Read = c1ReadNum > c2ReadNum;
                if (c1Read) {
                    c1Buffer.position(0);
                } else {
                    c1Buffer.position(limit);
                    c2Buffer.limit(c1Buffer.remaining());
                    c1ReadNum -= c2ReadNum;
                }
                if (c2Read) {
                    c2Buffer.position(0);
                } else {
                    c2Buffer.position(limit);
                    c1Buffer.limit(c2Buffer.remaining());
                    c2ReadNum -= c1ReadNum;
                }
                continue;
            }
            if (!c1Buffer.equals(c2Buffer)) {
                return false;
            }
            equals = c1Read = c2Read = true;
        }
    }

    /**
     * Tests if two seekable byte channel contents are equal starting at their respective current positions.
     * <p>
     * If the two channels have different sizes, no content comparison takes place, and this method returns false.
     * </p>
     *
     * @param channel1       A seekable byte channel.
     * @param channel2       Another seekable byte channel.
     * @param bufferCapacity The two internal buffer capacities, in bytes.
     * @return true if the contents of both RandomAccessFiles are equal, false otherwise.
     * @throws IOException if an I/O error occurs or the timeout is met.
     * @since 2.19.0
     */
    public static boolean contentEquals(final SeekableByteChannel channel1, final SeekableByteChannel channel2, final int bufferCapacity) throws IOException {
        // Short-circuit test
        if (Objects.equals(channel1, channel2)) {
            return true;
        }
        // Short-circuit test
        final long size1 = size(channel1);
        final long size2 = size(channel2);
        if (size1 != size2) {
            return false;
        }
        return size1 == 0 && size2 == 0 || contentEquals((ReadableByteChannel) channel1, channel2, bufferCapacity);
    }

    private static long size(final SeekableByteChannel channel) throws IOException {
        return channel != null ? channel.size() : 0;
    }

    /**
     * Don't instantiate.
     */
    private FileChannels() {
        // no-op
    }
}
