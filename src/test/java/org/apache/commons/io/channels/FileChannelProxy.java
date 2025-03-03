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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Proxies a FileChannel.
 */
class FileChannelProxy extends FileChannel {

    FileChannel fileChannel;

    FileChannelProxy(final FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public boolean equals(final Object o) {
        return fileChannel.equals(o);
    }

    @Override
    public void force(final boolean metaData) throws IOException {
        fileChannel.force(metaData);
    }

    @Override
    public int hashCode() {
        return fileChannel.hashCode();
    }

    @Override
    protected void implCloseChannel() throws IOException {
        fileChannel.close();
    }

    @Override
    public FileLock lock(final long position, final long size, final boolean shared) throws IOException {
        return fileChannel.lock(position, size, shared);
    }

    @Override
    public MappedByteBuffer map(final MapMode mode, final long position, final long size) throws IOException {
        return fileChannel.map(mode, position, size);
    }

    @Override
    public long position() throws IOException {
        return fileChannel.position();
    }

    @Override
    public FileChannel position(final long newPosition) throws IOException {
        return fileChannel.position(newPosition);
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return fileChannel.read(dst);
    }

    @Override
    public int read(final ByteBuffer dst, final long position) throws IOException {
        return fileChannel.read(dst, position);
    }

    @Override
    public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
        return fileChannel.read(dsts, offset, length);
    }

    @Override
    public long size() throws IOException {
        return fileChannel.size();
    }

    @Override
    public String toString() {
        return fileChannel.toString();
    }

    @Override
    public long transferFrom(final ReadableByteChannel src, final long position, final long count) throws IOException {
        return fileChannel.transferFrom(src, position, count);
    }

    @Override
    public long transferTo(final long position, final long count, final WritableByteChannel target) throws IOException {
        return fileChannel.transferTo(position, count, target);
    }

    @Override
    public FileChannel truncate(final long size) throws IOException {
        return fileChannel.truncate(size);
    }

    @Override
    public FileLock tryLock(final long position, final long size, final boolean shared) throws IOException {
        return fileChannel.tryLock(position, size, shared);
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        return fileChannel.write(src);
    }

    @Override
    public int write(final ByteBuffer src, final long position) throws IOException {
        return fileChannel.write(src, position);
    }

    @Override
    public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
        return fileChannel.write(srcs, offset, length);
    }
}
