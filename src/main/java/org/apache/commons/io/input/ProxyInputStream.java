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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.Erase;
import org.apache.commons.io.function.IOConsumer;

/**
 * A proxy stream which acts as a {@link FilterInputStream}, by passing all method calls on to the proxied stream, not changing which methods are called.
 * <p>
 * It is an alternative base class to {@link FilterInputStream} to increase reusability, because {@link FilterInputStream} changes the methods being called,
 * such as read(byte[]) to read(byte[], int, int).
 * </p>
 * <p>
 * In addition, this class allows you to:
 * </p>
 * <ul>
 * <li>notify a subclass that <em>n</em> bytes are about to be read through {@link #beforeRead(int)}</li>
 * <li>notify a subclass that <em>n</em> bytes were read through {@link #afterRead(int)}</li>
 * <li>notify a subclass that an exception was caught through {@link #handleIOException(IOException)}</li>
 * <li>{@link #unwrap()} itself</li>
 * </ul>
 */
public abstract class ProxyInputStream extends FilterInputStream {

    /**
     * Tracks whether {@link #close()} has been called or not.
     */
    private boolean closed;

    /**
     * Handles exceptions.
     */
    private final IOConsumer<IOException> exceptionHandler;

    /**
     * Constructs a new ProxyInputStream.
     *
     * @param proxy  the InputStream to proxy.
     */
    public ProxyInputStream(final InputStream proxy) {
        // the proxy is stored in a protected superclass variable named 'in'.
        this(proxy, Erase::rethrow);
    }

    /**
     * Constructs a new ProxyInputStream for testing.
     *
     * @param proxy  the InputStream to proxy.
     * @param exceptionHandler the exception handler.
     */
    ProxyInputStream(final InputStream proxy, final IOConsumer<IOException> exceptionHandler) {
        // the proxy is stored in a protected superclass instance variable named 'in'.
        super(proxy);
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Invoked by the {@code read} methods after the proxied call has returned
     * successfully. The number of bytes returned to the caller (or {@link IOUtils#EOF EOF} if
     * the end of stream was reached) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     * </p>
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add post-processing steps also to them.
     * </p>
     *
     * @since 2.0
     * @param n number of bytes read, or {@link IOUtils#EOF EOF} if the end of stream was reached.
     * @throws IOException if the post-processing fails in a subclass.
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void afterRead(final int n) throws IOException {
        // no-op default
    }

    /**
     * Invokes the delegate's {@link InputStream#available()} method.
     *
     * @return the number of available bytes, 0 if the stream is closed.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        if (in != null && !isClosed()) {
            try {
                return in.available();
            } catch (final IOException e) {
                handleIOException(e);
            }
        }
        return 0;
    }

    /**
     * Invoked by the {@code read} methods before the call is proxied. The number
     * of bytes that the caller wanted to read (1 for the {@link #read()}
     * method, buffer length for {@link #read(byte[])}, etc.) is given as
     * an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     * </p>
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add pre-processing steps also to them.
     * </p>
     *
     * @since 2.0
     * @param n number of bytes that the caller asked to be read.
     * @throws IOException if the pre-processing fails in a subclass.
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void beforeRead(final int n) throws IOException {
        // no-op default
    }

    /**
     * Checks if this instance is closed and throws an IOException if so.
     *
     * @throws IOException if this instance is closed.
     */
    void checkOpen() throws IOException {
        Input.checkOpen(!isClosed());
    }

    /**
     * Invokes the delegate's {@link InputStream#close()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        IOUtils.close(in, this::handleIOException);
        closed = true;
    }

    /**
     * Handles any IOExceptions thrown; by default, throws the given exception.
     * <p>
     * This method provides a point to implement custom exception
     * handling. The default behavior is to re-throw the exception.
     * </p>
     *
     * @param e The IOException thrown.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    protected void handleIOException(final IOException e) throws IOException {
        exceptionHandler.accept(e);
    }

    /**
     * Tests whether this instance is closed.
     *
     * @return whether this instance is closed.
     */
    boolean isClosed() {
        return closed;
    }

    /**
     * Invokes the delegate's {@link InputStream#mark(int)} method.
     *
     * @param readLimit read ahead limit.
     */
    @Override
    public synchronized void mark(final int readLimit) {
        if (in != null) {
            in.mark(readLimit);
        }
    }

    /**
     * Invokes the delegate's {@link InputStream#markSupported()} method.
     *
     * @return {@code true} if this stream instance supports the mark and reset methods; {@code false} otherwise.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public boolean markSupported() {
        return in != null && in.markSupported();
    }

    /**
     * Invokes the delegate's {@link InputStream#read()} method unless the stream is closed.
     *
     * @return the byte read or {@link IOUtils#EOF EOF} if we reached the end of stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        try {
            beforeRead(1);
            final int b = in.read();
            afterRead(b != EOF ? 1 : EOF);
            return b;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's {@link InputStream#read(byte[])} method.
     *
     * @param b the buffer to read the bytes into.
     * @return the number of bytes read or {@link IOUtils#EOF EOF} if we reached the end of stream.
     * @throws IOException
     *                     <ul>
     *                     <li>If the first byte cannot be read for any reason other than the end of the file,
     *                     <li>if the input stream has been closed, or</li>
     *                     <li>if some other I/O error occurs.</li>
     *                     </ul>
     */
    @Override
    public int read(final byte[] b) throws IOException {
        try {
            beforeRead(IOUtils.length(b));
            final int n = in.read(b);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's {@link InputStream#read(byte[], int, int)} method.
     *
     * @param b   the buffer to read the bytes into.
     * @param off The start offset.
     * @param len The number of bytes to read.
     * @return the number of bytes read or {@link IOUtils#EOF EOF} if we reached the end of stream.
     * @throws IOException
     *                     <ul>
     *                     <li>If the first byte cannot be read for any reason other than the end of the file,
     *                     <li>if the input stream has been closed, or</li>
     *                     <li>if some other I/O error occurs.</li>
     *                     </ul>
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        try {
            beforeRead(len);
            final int n = in.read(b, off, len);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's {@link InputStream#reset()} method.
     *
     * @throws IOException if this stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public synchronized void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Package-private for testing.
     *
     * @param in The input stream to set.
     */
    void setIn(final InputStream in) {
        this.in = in;
    }

    /**
     * Invokes the delegate's {@link InputStream#skip(long)} method.
     *
     * @param n the number of bytes to skip.
     * @return the actual number of bytes skipped.
     * @throws IOException if the stream does not support seek, or if some other I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        try {
            return in.skip(n);
        } catch (final IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    /**
     * Unwraps this instance by returning the underlying {@link InputStream}.
     * <p>
     * Use with caution; useful to query the underlying {@link InputStream}.
     * </p>
     *
     * @return the underlying {@link InputStream}.
     * @since 2.16.0
     */
    public InputStream unwrap() {
        return in;
    }

}
