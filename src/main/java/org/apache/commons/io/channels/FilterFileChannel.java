/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Filters a {@link FileChannel}.
 * <p>
 * A {@code FilterFileChannel} wraps some other channel, which it uses as its basic source of data, possibly transforming the data along the way or providing
 * additional functionality. The class {@code FilterFileChannel} itself simply overrides methods of {@code FileChannel} with versions that pass all requests to
 * the wrapped channel. Subclasses of {@code FilterFileChannel} may of course override any methods declared or inherited by {@code FilterFileChannel}, and may
 * also provide additional fields and methods.
 * </p>
 * <p>
 * You construct s simple instance with the {@link FilterFileChannel#FilterFileChannel(FileChannel) channel constructor} and more advanced instances through the
 * {@link Builder}.
 * </p>
 *
 * @since 2.22.0
 */
public class FilterFileChannel extends FileChannel {

    /**
     * Builds instances of {@link FilterFileChannel} for subclasses.
     *
     * @param <F> The {@link FilterFileChannel} type.
     * @param <C> The {@link Channel} type wrapped by the FilterChannel.
     * @param <B> The builder type.
     */
    public abstract static class AbstractBuilder<F extends FilterFileChannel, C extends FileChannel, B extends AbstractBuilder<F, C, B>>
            extends AbstractStreamBuilder<F, AbstractBuilder<F, C, B>> {

        /**
         * Constructs instance for subclasses.
         */
        protected AbstractBuilder() {
            // empty
        }
    }

    /**
     * Builds instances of {@link FilterFileChannel}.
     */
    public static class Builder extends AbstractBuilder<FilterFileChannel, FileChannel, Builder> {

        /**
         * Builds instances of {@link FilterChannel}.
         */
        protected Builder() {
            // empty
        }

        @Override
        public FilterFileChannel get() throws IOException {
            return new FilterFileChannel(this);
        }
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder forFilterFileChannel() {
        return new Builder();
    }

    final FileChannel fileChannel;

    private FilterFileChannel(final Builder builder) throws IOException {
        this.fileChannel = builder.getChannel(FileChannel.class);
    }

    /**
     * Constructs a new instance.
     *
     * @param fileChannel the file channel to wrap.
     */
    public FilterFileChannel(final FileChannel fileChannel) {
        this.fileChannel = Objects.requireNonNull(fileChannel, "fileChannel");
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

    /**
     * Unwraps this instance by returning the underlying {@link FileChannel}.
     * <p>
     * Use with caution.
     * </p>
     *
     * @return the underlying {@link FileChannel}.
     */
    public FileChannel unwrap() {
        return fileChannel;
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
