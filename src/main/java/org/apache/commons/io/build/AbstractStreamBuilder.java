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

package org.apache.commons.io.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;

/**
 * Abstracts building a typed instance of {@code T}.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractStreamBuilder<T, B extends AbstractStreamBuilder<T, B>> extends AbstractOriginSupplier<T, B> {

    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;

    private static final OpenOption[] DEFAULT_OPEN_OPTIONS = PathUtils.EMPTY_OPEN_OPTION_ARRAY;

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSize = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSizeDefault = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The maximum buffer size.
     */
    private int bufferSizeMax = DEFAULT_MAX_VALUE;

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charset = Charset.defaultCharset();

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charsetDefault = Charset.defaultCharset();

    private OpenOption[] openOptions = DEFAULT_OPEN_OPTIONS;

    /**
     * The default checking behavior for a buffer size request. Throws a {@link IllegalArgumentException} by default.
     */
    private final IntUnaryOperator defaultSizeChecker = size -> size > bufferSizeMax ? throwIae(size, bufferSizeMax) : size;

    /**
     * The checking behavior for a buffer size request.
     */
    private IntUnaryOperator bufferSizeChecker = defaultSizeChecker;

    /**
     * Applies the buffer size request.
     *
     * @param size the size request.
     * @return the size to use, usually the input, or can throw an unchecked exception, like {@link IllegalArgumentException}.
     */
    private int checkBufferSize(final int size) {
        return bufferSizeChecker.applyAsInt(size);
    }

    /**
     * Gets the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSizeDefault() {
        return bufferSizeDefault;
    }

    /**
     * Gets a CharSequence from the origin with a Charset.
     *
     * @return An input stream
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to a CharSequence.
     * @throws IOException                   if an I/O error occurs.
     * @see AbstractOrigin#getCharSequence(Charset)
     * @since 2.13.0
     */
    protected CharSequence getCharSequence() throws IOException {
        return checkOrigin().getCharSequence(getCharset());
    }

    /**
     * Gets the Charset, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset, defaults to {@link Charset#defaultCharset()}.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Gets the Charset default, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset default, defaults to {@link Charset#defaultCharset()}.
     */
    protected Charset getCharsetDefault() {
        return charsetDefault;
    }

    /**
     * Gets an InputStream from the origin with OpenOption[].
     *
     * @return An input stream
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
     * @throws IOException                   if an I/O error occurs.
     * @see AbstractOrigin#getInputStream(OpenOption...)
     * @see #getOpenOptions()
     * @since 2.13.0
     */
    protected InputStream getInputStream() throws IOException {
        return checkOrigin().getInputStream(getOpenOptions());
    }

    /**
     * Gets the OpenOption array.
     *
     * @return the OpenOption array.
     */
    protected OpenOption[] getOpenOptions() {
        return openOptions;
    }

    /**
     * Gets an OutputStream from the origin with OpenOption[].
     *
     * @return An OutputStream
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to an {@link OutputStream}.
     * @throws IOException                   if an I/O error occurs.
     * @see AbstractOrigin#getOutputStream(OpenOption...)
     * @see #getOpenOptions()
     * @since 2.13.0
     */
    protected OutputStream getOutputStream() throws IOException {
        return checkOrigin().getOutputStream(getOpenOptions());
    }

    /**
     * Gets a Path from the origin.
     *
     * @return A Path
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to a {@link Path}.
     * @see AbstractOrigin#getPath()
     * @since 2.13.0
     */
    protected Path getPath() {
        return checkOrigin().getPath();
    }

    /**
     * Gets a Reader from the origin with a Charset.
     *
     * @return A Reader
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to a {@link Reader}.
     * @throws IOException                   if an I/O error occurs.
     * @see AbstractOrigin#getReader(Charset)
     * @see #getCharset()
     * @since 2.16.0
     */
    protected Reader getReader() throws IOException {
        return checkOrigin().getReader(getCharset());
    }

    /**
     * Gets a Writer from the origin with an OpenOption[].
     *
     * @return An writer.
     * @throws IllegalStateException         if the {@code origin} is {@code null}.
     * @throws UnsupportedOperationException if the origin cannot be converted to a {@link Writer}.
     * @throws IOException                   if an I/O error occurs.
     * @see AbstractOrigin#getOutputStream(OpenOption...)
     * @see #getOpenOptions()
     * @since 2.13.0
     */
    protected Writer getWriter() throws IOException {
        return checkOrigin().getWriter(getCharset(), getOpenOptions());
    }

    /**
     * Sets the buffer size. Invalid input (bufferSize &lt;= 0) resets the value to its default.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size.
     * @return {@code this} instance.
     */
    public B setBufferSize(final int bufferSize) {
        this.bufferSize = checkBufferSize(bufferSize > 0 ? bufferSize : bufferSizeDefault);
        return asThis();
    }

    /**
     * Sets the buffer size.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size, null resets to the default.
     * @return {@code this} instance.
     */
    public B setBufferSize(final Integer bufferSize) {
        setBufferSize(bufferSize != null ? bufferSize : bufferSizeDefault);
        return asThis();
    }

    /**
     * Sets the buffer size checker function. Throws a {@link IllegalArgumentException} by default.
     *
     * @param bufferSizeChecker the buffer size checker function. null resets to the default behavior.
     * @return {@code this} instance.
     * @since 2.14.0
     */
    public B setBufferSizeChecker(final IntUnaryOperator bufferSizeChecker) {
        this.bufferSizeChecker = bufferSizeChecker != null ? bufferSizeChecker : defaultSizeChecker;
        return asThis();
    }

    /**
     * Sets the buffer size for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSizeDefault the buffer size, null resets to the default.
     * @return {@code this} instance.
     */
    protected B setBufferSizeDefault(final int bufferSizeDefault) {
        this.bufferSizeDefault = bufferSizeDefault;
        return asThis();
    }

    /**
     * The maximum buffer size checked by the buffer size checker. Values less or equal to 0, resets to the int max value. By default, if this value is
     * exceeded, this methods throws an {@link IllegalArgumentException}.
     *
     * @param bufferSizeMax maximum buffer size checked by the buffer size checker.
     * @return {@code this} instance.
     * @since 2.14.0
     */
    public B setBufferSizeMax(final int bufferSizeMax) {
        this.bufferSizeMax = bufferSizeMax > 0 ? bufferSizeMax : DEFAULT_MAX_VALUE;
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset, null resets to the default.
     * @return {@code this} instance.
     */
    public B setCharset(final Charset charset) {
        this.charset = Charsets.toCharset(charset, charsetDefault);
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset name, null resets to the default.
     * @return {@code this} instance.
     */
    public B setCharset(final String charset) {
        return setCharset(Charsets.toCharset(charset, charsetDefault));
    }

    /**
     * Sets the Charset default for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param defaultCharset the Charset name, null resets to the default.
     * @return {@code this} instance.
     */
    protected B setCharsetDefault(final Charset defaultCharset) {
        this.charsetDefault = defaultCharset;
        return asThis();
    }

    /**
     * Sets the OpenOption[].
     * <p>
     * Normally used with InputStream, OutputStream, and Writer.
     * </p>
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param openOptions the OpenOption[] name, null resets to the default.
     * @return {@code this} instance.
     * @since 2.13.0
     * @see #setInputStream(InputStream)
     * @see #setOutputStream(OutputStream)
     * @see #setWriter(Writer)
     */
    public B setOpenOptions(final OpenOption... openOptions) {
        this.openOptions = openOptions != null ? openOptions : DEFAULT_OPEN_OPTIONS;
        return asThis();
    }

    private int throwIae(final int size, final int max) {
        throw new IllegalArgumentException(String.format("Request %,d exceeds maximum %,d", size, max));
    }
}
