/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Automatically verifies a {@link Checksum} value once the stream is exhausted or the count threshold is reached.
 * <p>
 * If the {@link Checksum} does not meet the expected value when exhausted, then the input stream throws an
 * {@link IOException}.
 * </p>
 * <p>
 * If you do not need the verification or threshold feature, then use a plain {@link CheckedInputStream}.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @since 2.16.0
 */
public final class ChecksumInputStream extends CountingInputStream {

    // @formatter:off
    /**
     * Builds a new {@link ChecksumInputStream}.
     *
     * <p>
     * There is no default {@link Checksum}; you MUST provide one. This avoids any issue with a default {@link Checksum} being proven deficient or insecure
     * in the future.
     * </p>
     * <h2>Using NIO</h2>
     * <pre>{@code
     * ChecksumInputStream s = ChecksumInputStream.builder()
     *   .setPath(Paths.get("MyFile.xml"))
     *   .setChecksum(new CRC32())
     *   .setExpectedChecksumValue(12345)
     *   .get();
     * }</pre>
     * <h2>Using IO</h2>
     * <pre>{@code
     * ChecksumInputStream s = ChecksumInputStream.builder()
     *   .setFile(new File("MyFile.xml"))
     *   .setChecksum(new CRC32())
     *   .setExpectedChecksumValue(12345)
     *   .get();
     * }</pre>
     * <h2>Validating only part of an InputStream</h2>
     * <p>
     * The following validates the first 100 bytes of the given input.
     * </p>
     * <pre>{@code
     * ChecksumInputStream s = ChecksumInputStream.builder()
     *   .setPath(Paths.get("MyFile.xml"))
     *   .setChecksum(new CRC32())
     *   .setExpectedChecksumValue(12345)
     *   .setCountThreshold(100)
     *   .get();
     * }</pre>
     * <p>
     * To validate input <em>after</em> the beginning of a stream, build an instance with an InputStream starting where you want to validate.
     * </p>
     * <pre>{@code
     * InputStream inputStream = ...;
     * inputStream.read(...);
     * inputStream.skip(...);
     * ChecksumInputStream s = ChecksumInputStream.builder()
     *   .setInputStream(inputStream)
     *   .setChecksum(new CRC32())
     *   .setExpectedChecksumValue(12345)
     *   .setCountThreshold(100)
     *   .get();
     * }</pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<ChecksumInputStream, Builder> {

        /**
         * There is no default {@link Checksum}, you MUST provide one. This avoids any issue with a default {@link Checksum} being proven deficient or insecure
         * in the future.
         */
        private Checksum checksum;

        /**
         * The count threshold to limit how much input is consumed to update the {@link Checksum} before the input
         * stream validates its value.
         * <p>
         * By default, all input updates the {@link Checksum}.
         * </p>
         */
        private long countThreshold = -1;

        /**
         * The expected {@link Checksum} value once the stream is exhausted or the count threshold is reached.
         */
        private long expectedChecksumValue;

        /**
         * Builds a new {@link ChecksumInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link Checksum}</li>
         * <li>expectedChecksumValue</li>
         * <li>countThreshold</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public ChecksumInputStream get() throws IOException {
            return new ChecksumInputStream(getInputStream(), checksum, expectedChecksumValue, countThreshold);
        }

        /**
         * Sets the Checksum. There is no default {@link Checksum}, you MUST provide one. This avoids any issue with a default {@link Checksum} being proven
         * deficient or insecure in the future.
         *
         * @param checksum the Checksum.
         * @return {@code this} instance.
         */
        public Builder setChecksum(final Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Sets the count threshold to limit how much input is consumed to update the {@link Checksum} before the input
         * stream validates its value.
         * <p>
         * By default, all input updates the {@link Checksum}.
         * </p>
         *
         * @param countThreshold the count threshold. A negative number means the threshold is unbound.
         * @return {@code this} instance.
         */
        public Builder setCountThreshold(final long countThreshold) {
            this.countThreshold = countThreshold;
            return this;
        }

        /**
         * The expected {@link Checksum} value once the stream is exhausted or the count threshold is reached.
         *
         * @param expectedChecksumValue The expected Checksum value.
         * @return {@code this} instance.
         */
        public Builder setExpectedChecksumValue(final long expectedChecksumValue) {
            this.expectedChecksumValue = expectedChecksumValue;
            return this;
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

    /** The expected checksum. */
    private final long expectedChecksumValue;

    /**
     * The count threshold to limit how much input is consumed to update the {@link Checksum} before the input stream
     * validates its value.
     * <p>
     * By default, all input updates the {@link Checksum}.
     * </p>
     */
    private final long countThreshold;

    /**
     * Constructs a new instance.
     *
     * @param in                    the stream to wrap.
     * @param checksum              a Checksum implementation.
     * @param expectedChecksumValue the expected checksum.
     * @param countThreshold        the count threshold to limit how much input is consumed, a negative number means the
     *                              threshold is unbound.
     */
    private ChecksumInputStream(final InputStream in, final Checksum checksum, final long expectedChecksumValue,
            final long countThreshold) {
        super(new CheckedInputStream(in, Objects.requireNonNull(checksum, "checksum")));
        this.countThreshold = countThreshold;
        this.expectedChecksumValue = expectedChecksumValue;
    }

    @Override
    protected synchronized void afterRead(final int n) throws IOException {
        super.afterRead(n);
        if ((countThreshold > 0 && getByteCount() >= countThreshold || n == EOF)
                && expectedChecksumValue != getChecksum().getValue()) {
            // Validate when past the threshold or at EOF
            throw new IOException("Checksum verification failed.");
        }
    }

    /**
     * Gets the current checksum value.
     *
     * @return the current checksum value.
     */
    private Checksum getChecksum() {
        return ((CheckedInputStream) in).getChecksum();
    }

    /**
     * Gets the byte count remaining to read.
     *
     * @return bytes remaining to read, a negative number means the threshold is unbound.
     */
    public long getRemaining() {
        return countThreshold - getByteCount();
    }

}
