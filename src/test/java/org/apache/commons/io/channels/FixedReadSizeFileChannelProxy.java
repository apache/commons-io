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

/**
 * Always reads the same amount of bytes on each call or less.
 */
class FixedReadSizeFileChannelProxy extends FileChannelProxy {

    final int readSize;

    FixedReadSizeFileChannelProxy(final FileChannel fileChannel, final int readSize) {
        super(fileChannel);
        if (readSize < 1) {
            throw new IllegalArgumentException("readSize: " + readSize);
        }
        this.readSize = readSize;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        final int limit = dst.limit();
        dst.limit(Math.min(readSize, dst.limit()));
        final int read = super.read(dst);
        if (read > readSize) {
            throw new IllegalStateException("Programming error.");
        }
        dst.limit(limit);
        return read;
    }

    @Override
    public int read(final ByteBuffer dst, final long position) throws IOException {
        final int limit = dst.limit();
        dst.limit(Math.min(readSize, dst.limit()));
        final int read = super.read(dst, position);
        if (read > readSize) {
            throw new IllegalStateException("Programming error.");
        }
        dst.limit(limit);
        return read;
    }

    @Override
    public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
        throw new UnsupportedOperationException();
    }
}
