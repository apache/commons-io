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
import java.util.Objects;

import org.apache.commons.io.IOUtils;

/**
 * Works with {@link FileChannel}.
 *
 * @since 2.15.0
 */
public final class FileChannels {

    /**
     * Tests if two RandomAccessFiles contents are equal.
     *
     * @param channel1       A FileChannel.
     * @param channel2       Another FileChannel.
     * @param byteBufferSize The two internal buffer capacities, in bytes.
     * @return true if the contents of both RandomAccessFiles are equal, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean contentEquals(final FileChannel channel1, final FileChannel channel2, final int byteBufferSize) throws IOException {
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
        final ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(byteBufferSize);
        final ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(byteBufferSize);
        while (true) {
            final int read1 = channel1.read(byteBuffer1);
            final int read2 = channel2.read(byteBuffer2);
            byteBuffer1.clear();
            byteBuffer2.clear();
            if (read1 == IOUtils.EOF && read2 == IOUtils.EOF) {
                return byteBuffer1.equals(byteBuffer2);
            }
            if (read1 != read2) {
                return false;
            }
            if (!byteBuffer1.equals(byteBuffer2)) {
                return false;
            }
        }
    }

    private static long size(final FileChannel channel) throws IOException {
        return channel != null ? channel.size() : 0;
    }

    /**
     * Don't instantiate.
     */
    private FileChannels() {
        // no-op
    }
}
