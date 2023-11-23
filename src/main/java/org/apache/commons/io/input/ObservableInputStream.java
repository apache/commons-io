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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOConsumer;

/**
 * The {@link ObservableInputStream} allows, that an InputStream may be consumed by other receivers, apart from the
 * thread, which is reading it. The other consumers are implemented as instances of {@link Observer}.
 * <p>
 * A typical application may be the generation of a {@link java.security.MessageDigest} on the fly.
 * </p>
 * <p>
 * <em>Note</em>: The {@link ObservableInputStream} is <em>not</em> thread safe, as instances of InputStream usually
 * aren't. If you must access the stream from multiple threads, then synchronization, locking, or a similar means must
 * be used.
 * </p>
 *
 * @see MessageDigestInputStream
 */
public class ObservableInputStream extends ProxyInputStream {

    /**
     * Abstracts observer callback for {@link ObservableInputStream}s.
     */
    public static abstract class Observer {

        /**
         * Called to indicate that the {@link ObservableInputStream} has been closed.
         *
         * @throws IOException if an I/O error occurs.
         */
        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        public void closed() throws IOException {
            // noop
        }

        /**
         * Called to indicate that {@link InputStream#read(byte[])}, or {@link InputStream#read(byte[], int, int)} have
         * been called, and are about to invoke data.
         *
         * @param buffer The byte array, which has been passed to the read call, and where data has been stored.
         * @param offset The offset within the byte array, where data has been stored.
         * @param length The number of bytes, which have been stored in the byte array.
         * @throws IOException if an I/O error occurs.
         */
        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        public void data(final byte[] buffer, final int offset, final int length) throws IOException {
            // noop
        }

        /**
         * Called to indicate, that {@link InputStream#read()} has been invoked on the {@link ObservableInputStream},
         * and will return a value.
         *
         * @param value The value, which is being returned. This will never be -1 (EOF), because, in that case,
         *        {@link #finished()} will be invoked instead.
         * @throws IOException if an I/O error occurs.
         */
        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        public void data(final int value) throws IOException {
            // noop
        }

        /**
         * Called to indicate that an error occurred on the underlying stream.
         *
         * @param exception the exception to throw
         * @throws IOException if an I/O error occurs.
         */
        public void error(final IOException exception) throws IOException {
            throw exception;
        }

        /**
         * Called to indicate that EOF has been seen on the underlying stream. This method may be called multiple times,
         * if the reader keeps invoking either of the read methods, and they will consequently keep returning EOF.
         *
         * @throws IOException if an I/O error occurs.
         */
        @SuppressWarnings("unused") // Possibly thrown from subclasses.
        public void finished() throws IOException {
            // noop
        }
    }

    private final List<Observer> observers;

    /**
     * Constructs a new ObservableInputStream for the given InputStream.
     *
     * @param inputStream the input stream to observe.
     */
    public ObservableInputStream(final InputStream inputStream) {
        this(inputStream, new ArrayList<>());
    }

    /**
     * Constructs a new ObservableInputStream for the given InputStream.
     *
     * @param inputStream the input stream to observe.
     * @param observers List of observer callbacks.
     */
    private ObservableInputStream(final InputStream inputStream, final List<Observer> observers) {
        super(inputStream);
        this.observers = observers;
    }

    /**
     * Constructs a new ObservableInputStream for the given InputStream.
     *
     * @param inputStream the input stream to observe.
     * @param observers List of observer callbacks.
     * @since 2.9.0
     */
    public ObservableInputStream(final InputStream inputStream, final Observer... observers) {
        this(inputStream, Arrays.asList(observers));
    }

    /**
     * Adds an Observer.
     *
     * @param observer the observer to add.
     */
    public void add(final Observer observer) {
        observers.add(observer);
    }

    @Override
    public void close() throws IOException {
        IOException ioe = null;
        try {
            super.close();
        } catch (final IOException e) {
            ioe = e;
        }
        if (ioe == null) {
            noteClosed();
        } else {
            noteError(ioe);
        }
    }

    /**
     * Reads all data from the underlying {@link InputStream}, while notifying the observers.
     *
     * @throws IOException The underlying {@link InputStream}, or either of the observers has thrown an exception.
     */
    public void consume() throws IOException {
        IOUtils.consume(this);
    }

    private void forEachObserver(final IOConsumer<Observer> action) throws IOException {
        IOConsumer.forAll(action, observers);
    }

    /**
     * Gets a copy of currently registered observers.
     *
     * @return a copy of the list of currently registered observers.
     * @since 2.9.0
     */
    public List<Observer> getObservers() {
        return new ArrayList<>(observers);
    }

    /**
     * Notifies the observers by invoking {@link Observer#finished()}.
     *
     * @throws IOException Some observer has thrown an exception, which is being passed down.
     */
    protected void noteClosed() throws IOException {
        forEachObserver(Observer::closed);
    }

    /**
     * Notifies the observers by invoking {@link Observer#data(int)} with the given arguments.
     *
     * @param value Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being passed down.
     */
    protected void noteDataByte(final int value) throws IOException {
        forEachObserver(observer -> observer.data(value));
    }

    /**
     * Notifies the observers by invoking {@link Observer#data(byte[],int,int)} with the given arguments.
     *
     * @param buffer Passed to the observers.
     * @param offset Passed to the observers.
     * @param length Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being passed down.
     */
    protected void noteDataBytes(final byte[] buffer, final int offset, final int length) throws IOException {
        forEachObserver(observer -> observer.data(buffer, offset, length));
    }

    /**
     * Notifies the observers by invoking {@link Observer#error(IOException)} with the given argument.
     *
     * @param exception Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being passed down. This may be the same
     *         exception, which has been passed as an argument.
     */
    protected void noteError(final IOException exception) throws IOException {
        forEachObserver(observer -> observer.error(exception));
    }

    /**
     * Notifies the observers by invoking {@link Observer#finished()}.
     *
     * @throws IOException Some observer has thrown an exception, which is being passed down.
     */
    protected void noteFinished() throws IOException {
        forEachObserver(Observer::finished);
    }

    private void notify(final byte[] buffer, final int offset, final int result, final IOException ioe) throws IOException {
        if (ioe != null) {
            noteError(ioe);
            throw ioe;
        }
        if (result == EOF) {
            noteFinished();
        } else if (result > 0) {
            noteDataBytes(buffer, offset, result);
        }
    }

    @Override
    public int read() throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read();
        } catch (final IOException ex) {
            ioe = ex;
        }
        if (ioe != null) {
            noteError(ioe);
            throw ioe;
        }
        if (result == EOF) {
            noteFinished();
        } else {
            noteDataByte(result);
        }
        return result;
    }

    @Override
    public int read(final byte[] buffer) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(buffer);
        } catch (final IOException ex) {
            ioe = ex;
        }
        notify(buffer, 0, result, ioe);
        return result;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(buffer, offset, length);
        } catch (final IOException ex) {
            ioe = ex;
        }
        notify(buffer, offset, result, ioe);
        return result;
    }

    /**
     * Removes an Observer.
     *
     * @param observer the observer to remove
     */
    public void remove(final Observer observer) {
        observers.remove(observer);
    }

    /**
     * Removes all Observers.
     */
    public void removeAllObservers() {
        observers.clear();
    }

}
