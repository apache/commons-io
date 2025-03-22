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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * A Proxy stream which acts as expected, that is it passes the method
 * calls on to the proxied stream and doesn't change which methods are
 * being called. It is an alternative base class to FilterOutputStream
 * to increase reusability.
 * <p>
 * See the protected methods for ways in which a subclass can easily decorate
 * a stream with custom pre-, post- or error processing functionality.
 * </p>
 */
public class ProxyOutputStream extends FilterOutputStream {

    /**
     * Builds instances of {@link ProxyOutputStream}.
     * <p>
     * This class does not provide a convenience static {@code builder()} method so that subclasses can.
     * </p>
     *
     * @since 2.19.0
     */
    public static class Builder extends AbstractStreamBuilder<ProxyOutputStream, Builder> {

        /**
         * Constructs a new builder of {@link ProxyOutputStream}.
         */
        public Builder() {
            // empty
        }

        /**
         * Builds a new {@link ProxyOutputStream}.
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getOutputStream()} is the target aspect.</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link OutputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link OutputStream} using {@link #getOutputStream()}.
         * @see #getOutputStream()
         * @see #getUnchecked()
         */
        @SuppressWarnings("resource") // caller closes
        @Override
        public ProxyOutputStream get() throws IOException {
            return new ProxyOutputStream(getOutputStream());
        }

    }

    /**
     * Constructs a new ProxyOutputStream.
     *
     * @param delegate  the OutputStream to delegate to
     */
    public ProxyOutputStream(final OutputStream delegate) {
        // the delegate is stored in a protected superclass variable named 'out'
        super(delegate);
    }

    /**
     * Invoked by the write methods after the proxied call has returned
     * successfully. The number of bytes written (1 for the
     * {@link #write(int)} method, buffer length for {@link #write(byte[])},
     * etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the write methods.
     * The default implementation does nothing.
     *
     * @param n number of bytes written
     * @throws IOException if the post-processing fails
     * @since 2.0
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void afterWrite(final int n) throws IOException {
        // noop
    }

    /**
     * Invoked by the write methods before the call is proxied. The number
     * of bytes to be written (1 for the {@link #write(int)} method, buffer
     * length for {@link #write(byte[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing
     * functionality without having to override all the write methods.
     * The default implementation does nothing.
     *
     * @param n number of bytes to be written
     * @throws IOException if the pre-processing fails
     * @since 2.0
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void beforeWrite(final int n) throws IOException {
        // noop
    }

    /**
     * Invokes the delegate's {@code close()} method.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        IOUtils.close(out, this::handleIOException);
    }

    /**
     * Invokes the delegate's {@code flush()} method.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception
     * handling. The default behavior is to re-throw the exception.
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    protected void handleIOException(final IOException e) throws IOException {
        throw e;
    }

    /**
     * Sets the underlying output stream.
     *
     * @param out the underlying output stream.
     * @return this instance.
     * @since 2.19.0
     */
    public ProxyOutputStream setReference(final OutputStream out) {
        this.out = out;
        return this;
    }

    /**
     * Unwraps this instance by returning the underlying {@link OutputStream}.
     * <p>
     * Use with caution; useful to query the underlying {@link OutputStream}.
     * </p>
     *
     * @return the underlying {@link OutputStream}.
     */
    OutputStream unwrap() {
        return out;
    }

    /**
     * Invokes the delegate's {@code write(byte[])} method.
     * @param bts the bytes to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] bts) throws IOException {
        try {
            final int len = IOUtils.length(bts);
            beforeWrite(len);
            out.write(bts);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(byte[])} method.
     * @param bts the bytes to write
     * @param st The start offset
     * @param end The number of bytes to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] bts, final int st, final int end) throws IOException {
        try {
            beforeWrite(end);
            out.write(bts, st, end);
            afterWrite(end);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(int)} method.
     * @param idx the byte to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final int idx) throws IOException {
        try {
            beforeWrite(1);
            out.write(idx);
            afterWrite(1);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

}
