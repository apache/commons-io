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
import java.io.Reader;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.io.function.Uncheck;

/**
 * Provides the contents of multiple {@link Reader}s in sequence.
 * <p>
 * Like {@link SequenceInputStream} but for {@link Reader} arguments.
 * </p>
 *
 * @since 2.7
 */
public class SequenceReader extends Reader {

    private Reader reader;
    private final Iterator<? extends Reader> readers;

    /**
     * Constructs a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(final Iterable<? extends Reader> readers) {
        this.readers = Objects.requireNonNull(readers, "readers").iterator();
        this.reader = Uncheck.get(this::nextReader);
    }

    /**
     * Constructs a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(final Reader... readers) {
        this(Arrays.asList(readers));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        do { // NOPMD
             // empty
        } while (nextReader() != null);
    }

    /**
     * Returns the next available reader or null if done.
     *
     * @return the next available reader or null.
     * @throws IOException IOException  If an I/O error occurs.
     */
    private Reader nextReader() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (readers.hasNext()) {
            reader = readers.next();
        } else {
            reader = null;
        }
        return reader;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read() throws IOException {
        int c = EOF;
        while (reader != null) {
            c = reader.read();
            if (c != EOF) {
                break;
            }
            nextReader();
        }
        return c;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read()
     */
    @Override
    public int read(final char[] cbuf, int off, int len) throws IOException {
        Objects.requireNonNull(cbuf, "cbuf");
        if (len < 0 || off < 0 || off + len > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length + ", offset=" + off + ", length=" + len);
        }
        int count = 0;
        while (reader != null) {
            final int readLen = reader.read(cbuf, off, len);
            if (readLen == EOF) {
                nextReader();
            } else {
                count += readLen;
                off += readLen;
                len -= readLen;
                if (len <= 0) {
                    break;
                }
            }
        }
        if (count > 0) {
            return count;
        }
        return EOF;
    }
}
