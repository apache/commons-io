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

/**
 * This is a stream that will only supply bytes up to a certain length - if its
 * position goes above that, it will stop.
 * <p>
 * This is useful to wrap ServletInputStreams. The ServletInputStream will block
 * if you try to read content from it that isn't there, because it doesn't know
 * whether the content hasn't arrived yet or whether the content has finished.
 * So, one of these, initialized with the Content-length sent in the
 * ServletInputStream's header, will stop it blocking, providing it's been sent
 * with a correct content length.
 *
 * @author InigoSurguy
 */
public class BoundedInputStream extends InputStream {

    /** the wrapped input stream */
    private final InputStream in;

    /** the max length to provide */
    private final int max;

    /** the number of bytes already returned */
    private int pos = 0;

    /** the marked position */
    private int mark = -1;

    /** flag if close shoud be propagated */
    private boolean propagateClose = true;

    /**
     * Creates a new <code>BoundedInputStream</code> that wraps the given input
     * stream and limits it to a certain size.
     *
     * @param in The wrapped input stream
     * @param size The maximum number of bytes to return
     */
    public BoundedInputStream(InputStream in, long size) {
        // Some badly designed methods - eg the servlet API - overload length
        // such that "-1" means stream finished
        this.max = (int) size;
        this.in = in;
    }

    public BoundedInputStream(InputStream in) {
        this(in, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (max>=0 && pos==max) {
            return -1;
        }
        int result = in.read();
        pos++;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (max>=0 && pos>=max) {
            return -1;
        }
        int maxRead = max>=0 ? Math.min(len, max-pos) : len;
        int bytesRead = in.read(b, off, maxRead);

        if (bytesRead==-1) {
            return -1;
        }

        pos+=bytesRead;
        return bytesRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException {
        long toSkip = max>=0 ? Math.min(n, max-pos) : n;
        long skippedBytes = in.skip(toSkip);
        pos+=skippedBytes;
        return skippedBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        if (max>=0 && pos>=max) {
            return 0;
        }
        return in.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return in.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (propagateClose) {
            in.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        pos = mark;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = pos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    public boolean isPropagateClose() {
        return propagateClose;
    }

    public void setPropagateClose(boolean propagateClose) {
        this.propagateClose = propagateClose;
    }
}
