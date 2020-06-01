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
package org.apache.commons.io.input.buffer;

import java.io.IOException;
import java.io.Reader;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.EOF;

/**
 * A BufferedReader class who does not care about thread safety, but very much faster.
 *
 * Should be able to replace java.io.BufferedReader in nearly every use-cases when you
 * need the Reader be buffered, but do not need it have thread safety.
 */
public class UnsynchronizedBufferedReader extends Reader {
    private final Reader reader;
    private final char[] charBuffer;

    private int nowIndex;
    private int nowLimit;

    /**
     * Creates a new instance, which filters the given reader, and
     * uses the given buffer size.
     *
     * @param reader The original reader, which is being buffered.
     * @param charBufferSize size of the buffer.
     */
    public UnsynchronizedBufferedReader(Reader reader, int charBufferSize) {
        this(reader, new char[charBufferSize]);
    }

    /**
     * Creates a new instance, which filters the given reader, and
     * uses IOUtils.DEFAULT_BUFFER_SIZE.
     *
     * @param reader The original reader, which is being buffered.
     * @see IOUtils#DEFAULT_BUFFER_SIZE
     */
    public UnsynchronizedBufferedReader(Reader reader) {
        this(reader, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new instance, which filters the given reader, and
     * uses the given buffer.
     *
     * @param reader The original reader, which is being buffered.
     * @param charBuffer buffer used.
     */
    public UnsynchronizedBufferedReader(Reader reader, char[] charBuffer) {
        this.reader = reader;
        this.charBuffer = charBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        int currentBufferSize = this.nowLimit - this.nowIndex;
        if (currentBufferSize == 0) {
            int readLength;
            do {
                readLength = this.reader.read(this.charBuffer, 0, this.charBuffer.length);
            } while (readLength == 0);
            if (readLength == EOF) {
                return EOF;
            }
            this.nowLimit = readLength;
            this.nowIndex = 0;
            currentBufferSize = this.nowLimit - this.nowIndex;
        }
        if (currentBufferSize <= len) {
            System.arraycopy(this.charBuffer, this.nowIndex, cbuf, off, currentBufferSize);
            this.nowLimit = this.nowIndex = 0;
            return currentBufferSize;
        } else {
            System.arraycopy(this.charBuffer, this.nowIndex, cbuf, off, len);
            this.nowIndex += len;
            return len;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int res = this.peek();
        if (res != EOF) {
            eat();
        }
        return res;
    }

    /**
     * see the next char, but not mark it as read.
     *
     * @return the next char
     * @throws IOException by reader.read()
     * @see #read()
     */
    public int peek() throws IOException {
        int currentBufferSize = this.nowLimit - this.nowIndex;
        if (currentBufferSize == 0) {
            int readLength;
            do {
                readLength = this.reader.read(this.charBuffer, 0, this.charBuffer.length);
            } while (readLength == 0);
            if (readLength == EOF) {
                return EOF;
            }
            this.nowLimit = readLength;
            this.nowIndex = 0;
            return this.charBuffer[0];
        }
        return this.charBuffer[this.nowIndex];
    }

    /**
     * mark the current char as read.
     * must be used after invoke peek.
     *
     * @see #read()
     * @see #peek()
     */
    public void eat() {
        this.nowIndex++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
        }
    }

    /**
     * getter for this.reader
     * @return this.reader
     */
    public Reader getReader() {
        return this.reader;
    }

    /**
     * getter for this.charBuffer
     * @return this.charBuffer
     */
    public char[] getCharBuffer() {
        return this.charBuffer;
    }

    /**
     * getter for this.nowIndex
     * @return this.nowIndex
     */
    public int getNowIndex() {
        return this.nowIndex;
    }

    /**
     * setter for this.nowIndex
     * @param nowIndex this.nowIndex
     */
    public void setNowIndex(int nowIndex) {
        this.nowIndex = nowIndex;
    }

    /**
     * getter for this.nowLimit
     * @return this.nowLimit
     */
    public int getNowLimit() {
        return this.nowLimit;
    }

    /**
     * setter for this.nowLimit
     * @param nowLimit this.nowLimit
     */
    public void setNowLimit(int nowLimit) {
        this.nowLimit = nowLimit;
    }
}
