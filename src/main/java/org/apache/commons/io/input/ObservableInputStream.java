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
import java.util.ArrayList;
import java.util.List;


/**
 * The {@link ObservableInputStream} allows, that an InputStream may be consumed
 * by other receivers, apart from the thread, which is reading it.
 * The other consumers are implemented as instances of {@link Observer}. A
 * typical application may be the generation of a {@link MessageDigest} on the
 * fly.
 * {@code Note}: The {@link ObservableInputStream} is <em>not</em> thread safe,
 * as instances of InputStream usually aren't.
 * If you must access the stream from multiple threads, then synchronization, locking,
 * or a similar means must be used.
 * @see MessageDigestCalculatingInputStream
 */
public class ObservableInputStream extends ProxyInputStream {
    public static abstract class Observer {
        /** Called to indicate, that {@link InputStream#read()} has been invoked
         * on the {@link ObservableInputStream}, and will return a value.
         * @param pByte The value, which is being returned. This will never be -1 (EOF),
         *    because, in that case, {link #finished()} will be invoked instead.
         */
        void data(int pByte) throws IOException {}
        /** Called to indicate, that {@link InputStream#read(byte[])}, or
         * {@link InputStream#read(byte[], int, int)} have been called, and are about to
         * invoke data.
         * @param pBuffer The byte array, which has been passed to the read call, and where
         *   data has been stored.
         * @param pOffset The offset within the byte array, where data has been stored.
         * @param pLength The number of bytes, which have been stored in the byte array.
         */
        void data(byte[] pBuffer, int pOffset, int pLength) throws IOException {}
        /** Called to indicate, that EOF has been seen on the underlying stream.
         * This method may be called multiple times, if the reader keeps invoking
         * either of the read methods, and they will consequently keep returning
         * EOF.
         */
        void finished() throws IOException {}
        /** Called to indicate, that the {@link ObservableInputStream} has been closed.
         */
        void closed() throws IOException {}
        /**
         * Called to indicate, that an error occurred on the underlying stream.
         */
        void error(IOException pException) throws IOException { throw pException; }
    }

    private final List<Observer> observers = new ArrayList<>();
    
    public ObservableInputStream(InputStream pProxy) {
        super(pProxy);
    }

    public void add(Observer pObserver) {
        observers.add(pObserver);
    }

    public void remove(Observer pObserver) {
        observers.remove(pObserver);
    }

    public void removeAllObservers() {
        observers.clear();
    }

    @Override
    public int read() throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read();
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else {
            noteDataByte(result);
        }
        return result;
    }

    @Override
    public int read(byte[] pBuffer) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(pBuffer);
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else if (result > 0) {
            noteDataBytes(pBuffer, 0, result);
        }
        return result;
    }

    @Override
    public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(pBuffer, pOffset, pLength);
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else if (result > 0) {
            noteDataBytes(pBuffer, pOffset, result);
        }
        return result;
    }

    /** Notifies the observers by invoking {@link Observer#data(byte[],int,int)}
     * with the given arguments.
     * @param pBuffer Passed to the observers.
     * @param pOffset Passed to the observers.
     * @param pLength Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being
     *   passed down.
     */
    protected void noteDataBytes(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        for (Observer observer : getObservers()) {
            observer.data(pBuffer, pOffset, pLength);
        }
    }

    /** Notifies the observers by invoking {@link Observer#finished()}.
     * @throws IOException Some observer has thrown an exception, which is being
     *   passed down.
     */
    protected void noteFinished() throws IOException {
        for (Observer observer : getObservers()) {
            observer.finished();
        }
    }

    /** Notifies the observers by invoking {@link Observer#data(int)}
     * with the given arguments.
     * @param pDataByte Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being
     *   passed down.
     */
    protected void noteDataByte(int pDataByte) throws IOException {
        for (Observer observer : getObservers()) {
            observer.data(pDataByte);
        }
    }

    /** Notifies the observers by invoking {@link Observer#error(IOException)}
     * with the given argument.
     * @param pException Passed to the observers.
     * @throws IOException Some observer has thrown an exception, which is being
     *   passed down. This may be the same exception, which has been passed as an
     *   argument.
     */
    protected void noteError(IOException pException) throws IOException {
        for (Observer observer : getObservers()) {
            observer.error(pException);
        }
    }

    /** Notifies the observers by invoking {@link Observer#finished()}.
     * @throws IOException Some observer has thrown an exception, which is being
     *   passed down.
     */
    protected void noteClosed() throws IOException {
        for (Observer observer : getObservers()) {
            observer.closed();
        }
    }

    protected List<Observer> getObservers() {
        return observers;
    }

    @Override
    public void close() throws IOException {
        IOException ioe = null;
        try {
            super.close();
        } catch (IOException e) {
            ioe = e;
        }
        if (ioe == null) {
            noteClosed();
        } else {
            noteError(ioe);
        }
    }

    /** Reads all data from the underlying {@link InputStream}, while notifying the
     * observers.
     * @throws IOException The underlying {@link InputStream}, or either of the
     *   observers has thrown an exception.
     */
    public void consume() throws IOException {
        final byte[] buffer = new byte[8192];
        for (;;) {
            final int res = read(buffer);
            if (res == -1) {
                return;
            }
        }
    }
    
}
