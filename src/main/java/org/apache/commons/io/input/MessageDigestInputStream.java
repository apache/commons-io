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
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is an example for using an {@link ObservableInputStream}. It creates its own {@link org.apache.commons.io.input.ObservableInputStream.Observer},
 * which calculates a checksum using a {@link MessageDigest}, for example, a SHA-512 sum.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * See the MessageDigest section in the <a href= "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest"> Java
 * Cryptography Architecture Standard Algorithm Name Documentation</a> for information about standard algorithm names.
 * </p>
 * <p>
 * You must specify a message digest algorithm name or instance.
 * </p>
 * <p>
 * <em>Note</em>: Neither {@link ObservableInputStream}, nor {@link MessageDigest}, are thread safe, so is {@link MessageDigestInputStream}.
 * </p>
 *
 * @see Builder
 * @since 2.15.0
 */
public final class MessageDigestInputStream extends ObservableInputStream {

    // @formatter:off
    /**
     * Builds new {@link MessageDigestInputStream}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * MessageDigestInputStream s = MessageDigestInputStream.builder()
     *   .setPath(path)
     *   .setMessageDigest("SHA-512")
     *   .get();}
     * </pre>
     * <p>
     * You must specify a message digest algorithm name or instance.
     * </p>
     * <p>
     * <em>The MD5 cryptographic algorithm is weak and should not be used.</em>
     * </p>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractBuilder<Builder> {

        /**
         * No default by design, call MUST set one.
         */
        private MessageDigest messageDigest;

        /**
         * Constructs a new builder of {@link MessageDigestInputStream}.
         */
        public Builder() {
            // empty
        }

        /**
         * Builds new {@link MessageDigestInputStream}.
         * <p>
         * You must set an aspect that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()} gets the target aspect.</li>
         * <li>{@link MessageDigest}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws NullPointerException if messageDigest is null.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link InputStream} using {@link #getInputStream()}.
         * @see #getInputStream()
         * @see #getUnchecked()
         */
        @Override
        public MessageDigestInputStream get() throws IOException {
            setObservers(Arrays.asList(new MessageDigestMaintainingObserver(messageDigest)));
            return new MessageDigestInputStream(this);
        }

        /**
         * Sets the message digest.
         * <p>
         * <em>The MD5 cryptographic algorithm is weak and should not be used.</em>
         * </p>
         *
         * @param messageDigest the message digest.
         * @return {@code this} instance.
         */
        public Builder setMessageDigest(final MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
            return this;
        }

        /**
         * Sets the name of the name of the message digest algorithm.
         * <p>
         * <em>The MD5 cryptographic algorithm is weak and should not be used.</em>
         * </p>
         *
         * @param algorithm the name of the algorithm. See the MessageDigest section in the
         *                  <a href= "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest"> Java Cryptography
         *                  Architecture Standard Algorithm Name Documentation</a> for information about standard algorithm names.
         * @return {@code this} instance.
         * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified algorithm.
         */
        public Builder setMessageDigest(final String algorithm) throws NoSuchAlgorithmException {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            return this;
        }

    }

    /**
     * Maintains the message digest.
     */
    public static class MessageDigestMaintainingObserver extends Observer {

        private final MessageDigest messageDigest;

        /**
         * Constructs an MessageDigestMaintainingObserver for the given MessageDigest.
         *
         * @param messageDigest the message digest to use
         * @throws NullPointerException if messageDigest is null.
         */
        public MessageDigestMaintainingObserver(final MessageDigest messageDigest) {
            this.messageDigest = Objects.requireNonNull(messageDigest, "messageDigest");
        }

        @Override
        public void data(final byte[] input, final int offset, final int length) throws IOException {
            messageDigest.update(input, offset, length);
        }

        @Override
        public void data(final int input) throws IOException {
            messageDigest.update((byte) input);
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

    /**
     * A non-null MessageDigest.
     */
    private final MessageDigest messageDigest;

    /**
     * Constructs a new instance, which calculates a signature on the given stream, using the given {@link MessageDigest}.
     * <p>
     * The MD5 cryptographic algorithm is weak and should not be used.
     * </p>
     *
     * @param builder A builder use to get the stream to calculate the message digest and the message digest to use
     * @throws NullPointerException if messageDigest is null.
     */
    private MessageDigestInputStream(final Builder builder) throws IOException {
        super(builder);
        this.messageDigest = Objects.requireNonNull(builder.messageDigest, "builder.messageDigest");
    }

    /**
     * Gets the {@link MessageDigest}, which is being used for generating the checksum, never null.
     * <p>
     * <em>Note</em>: The checksum will only reflect the data, which has been read so far. This is probably not, what you expect. Make sure, that the complete
     * data has been read, if that is what you want. The easiest way to do so is by invoking {@link #consume()}.
     * </p>
     *
     * @return the message digest used, never null.
     */
    public MessageDigest getMessageDigest() {
        return messageDigest;
    }
}
