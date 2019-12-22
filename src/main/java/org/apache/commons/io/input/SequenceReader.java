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
import java.io.Reader;
import java.util.List;

import static org.apache.commons.io.IOUtils.EOF;

/**
 * Provides the contents of multiple Readers in sequence.
 *
 * @since 2.7
 */
public class SequenceReader extends Reader {
    private Reader[] fReaders;
    private int fReadersOffset;

    /**
     * Construct a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(List<? extends Reader> readers) {
        final int size = readers.size();
        fReaders = new Reader[size];
        for (int i = 0; i < size; i++) {
            fReaders[i] = readers.get(i);
        }
    }

    /**
     * Construct a new instance with readers
     *
     * @param readers the readers to read
     */
    public SequenceReader(Reader... readers) {
        fReaders = readers;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        fReaders = new Reader[0];
        fReadersOffset = 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(final char[] cbuf, int off, int len) throws IOException {
        if (cbuf == null) {
            throw new NullPointerException("Character array is missing");
        }
        if (len < 0 || off < 0 || off + len > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length +
                    ", offset=" + off + ", length=" + len);
        }
        int count = 0;

        while (fReadersOffset < fReaders.length) {
            int readLen = fReaders[fReadersOffset].read(cbuf, off, len);
            if (readLen == EOF) {
                // release unused reference to free memory
                fReaders[fReadersOffset] = null;
                fReadersOffset++;
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

    /*
     * (non-Javadoc)
     *
     * @see java.io.Reader#read()
     */
    @Override
    public int read() throws IOException {
        int c = EOF;
        for (int i = fReadersOffset; i < fReaders.length; i++) {
            c = fReaders[i].read();
            if (c == EOF) {
                // release unused reference to free memory
                fReaders[i] = null;
                fReadersOffset++;
            } else {
                break;
            }
        }
        return c;
    }
}
