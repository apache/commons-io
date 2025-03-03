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
     * Tests if two FileChannel contents are equal starting at their respective current positions.
     *
     * @param channel1       A FileChannel.
     * @param channel2       Another FileChannel.
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
     * <p>
     * If a file channel is a non-blocking file channel, it may return 0 bytes read for any given call. In order to avoid waiting forever when trying again, a
     * timeout Duration can be specified, which when met, throws an IOException.
     * </p>
     *
     * @param channel1       A readable byte channel.
     * @param channel2       Another readable byte channel.
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
        if (size1 == 0 && size2 == 0) {
            return true;
        }
        // Dig in and do the work
        final ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(bufferCapacity);
        final ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(bufferCapacity);
        int numRead1 = 0;
        int numRead2 = 0;
        boolean read0On1 = false;
        boolean read0On2 = false;
        while (true) {
            if (!read0On2) {
                numRead1 = channel1.read(byteBuffer1);
                byteBuffer1.clear();
                read0On1 = numRead1 == 0;
            }
            if (!read0On1) {
                numRead2 = channel2.read(byteBuffer2);
                byteBuffer2.clear();
                read0On2 = numRead2 == 0;
            }
            if (numRead1 == IOUtils.EOF && numRead2 == IOUtils.EOF) {
                return byteBuffer1.equals(byteBuffer2);
            }
            if (numRead1 == 0 || numRead2 == 0) {
                // 0 may be returned from a non-blocking channel.
                Thread.yield();
                continue;
            }
            if (numRead1 != numRead2) {
                return false;
            }
            if (!byteBuffer1.equals(byteBuffer2)) {
                return false;
            }
        }
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
