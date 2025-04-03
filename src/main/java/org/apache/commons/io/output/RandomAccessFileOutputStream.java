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

package org.apache.commons.io.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * An {@link OutputStream} that writes to a {@link RandomAccessFile}.
 *
 * @since 2.18.0
 */
public final class RandomAccessFileOutputStream extends OutputStream {

    // @formatter:off
    /**
     * Builds a new {@link RandomAccessFileOutputStream}.
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * RandomAccessFileOutputStream s = RandomAccessFileOutputStream.builder()
     *   .setFile("myfile.txt")
     *   .setOpenOptions(StandardOpenOption.SYNC)
     *   .get();}
     * </pre>
     * <p>
     * The only super's aspect used is buffer size.
     * </p>
     *
     * @see #get()
     */
    // @formatter:on
    public static final class Builder extends AbstractStreamBuilder<RandomAccessFileOutputStream, Builder> {

        /**
         * Use {@link RandomAccessFileOutputStream#builder()}.
         */
        private Builder() {
            setOpenOptions(StandardOpenOption.WRITE);
        }

        /**
         * Builds a new {@link RandomAccessFileOutputStream}.
         * <p>
         * You must set an aspect that supports {@link RandomAccessFile} or {@link File}, otherwise, this method throws an exception. Only set one of
         * RandomAccessFile or an origin that can be converted to a File.
         * </p>
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link RandomAccessFile} is the target aspect.</li>
         * <li>{@link File}</li>
         * <li>closeOnClose</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws IllegalStateException         if both RandomAccessFile and origin are set.
         * @throws UnsupportedOperationException if the origin cannot be converted to a {@link RandomAccessFile}.
         * @throws IOException                   if an I/O error occurs converting to an {@link RandomAccessFile} using {@link #getRandomAccessFile()}.
         * @see AbstractOrigin#getFile()
         * @see #getUnchecked()
         */
        @Override
        public RandomAccessFileOutputStream get() throws IOException {
            return new RandomAccessFileOutputStream(this);
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private final RandomAccessFile randomAccessFile;

    private RandomAccessFileOutputStream(final Builder builder) throws IOException {
        this.randomAccessFile = builder.getRandomAccessFile();
    }

    @Override
    public void close() throws IOException {
        this.randomAccessFile.close();
        super.close();
    }

    @SuppressWarnings("resource")
    @Override
    public void flush() throws IOException {
        randomAccessFile.getChannel().force(true);
        super.flush();
    }

    /**
     * Gets the underlying random access file.
     *
     * @return the underlying random access file.
     * @since 2.19.0
     */
    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    @Override
    public void write(final int b) throws IOException {
        randomAccessFile.write(b);
    }

}
