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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;

/**
 * A NonThreadSafeButFastBufferedReader who use some filters to make line ends unified.
 *
 * <ul>
 *   <li>"\r\n" in original reader will become "\n",
 *   <li>"\n" in original reader will become "\n",
 *   <li>"\r" with normal character behind in original reader will become "\n",
 *   <li>if the original reader ends with "\r" then it will become "\n".
 * </ul>
 */
public class LineEndUnifiedBufferedReader extends Reader {

    private final Reader reader;

    private final char[] charBuffer;

    private int nowIndex = 0;

    private int nowLimit = 0;

    private boolean cachedCR;

    /**
     * Creates a new instance, which filters the given reader, and
     * uses the given buffer size.
     *
     * @param reader The original reader, which is being buffered.
     * @param charBufferSize size of the buffer.
     */
    public LineEndUnifiedBufferedReader(Reader reader, int charBufferSize) {
        this(reader, new char[charBufferSize]);
    }

    /**
     * Creates a new instance, which filters the given reader, and
     * uses IOUtils.DEFAULT_BUFFER_SIZE.
     *
     * @param reader The original reader, which is being buffered.
     * @see IOUtils#DEFAULT_BUFFER_SIZE
     */
    public LineEndUnifiedBufferedReader(Reader reader) {
        this(reader, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new instance, which filters the given reader, and
     * uses the given buffer.
     *
     * @param reader The original reader, which is being buffered.
     * @param charBuffer buffer used.
     */
    public LineEndUnifiedBufferedReader(Reader reader, char[] charBuffer) {
        this.reader = reader;
        this.charBuffer = charBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws UncheckedIOException {
        if (len <= 0) {
            return 0;
        }
        final char[] charBufferLocal = this.getCharBuffer();
        final Reader readerLocal = this.getReader();
        int nowLimitLocal = this.getNowLimit();
        int nowIndexLocal = this.getNowIndex();
        int currentBufferSize = nowLimitLocal - nowIndexLocal;

        if (currentBufferSize == 0) {
            nowLimitLocal = nowIndexLocal = 0;
            if (this.cachedCR) {
                charBufferLocal[nowLimitLocal++] = '\r';
            }

            int readLength;
            do {
                try {
                    readLength = readerLocal.read(charBufferLocal, 0, charBufferLocal.length - nowLimitLocal);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            } while (readLength == 0);
            if (readLength == EOF) {
                if (this.cachedCR) {
                    this.cachedCR = false;
                    cbuf[off] = charBufferLocal[nowIndexLocal++] = '\n';
                    this.setNowIndex(nowIndexLocal);
                    this.setNowLimit(nowLimitLocal);
                    return 1;
                }
                this.setNowIndex(nowIndexLocal);
                this.setNowLimit(nowLimitLocal);
                return EOF;
            }
            nowLimitLocal += readLength;

            if (charBufferLocal[nowLimitLocal - 1] == '\r') {
                --nowLimitLocal;
                this.cachedCR = true;
            } else {
                this.cachedCR = false;
            }
            this.setNowLimit(nowLimitLocal);
            if (nowLimitLocal == 0) {
                this.setNowIndex(nowIndexLocal);
                return 0;
            }
            this.filter();
            nowLimitLocal = this.getNowLimit();
            nowIndexLocal = this.getNowIndex();
            currentBufferSize = nowLimitLocal - nowIndexLocal;
        }
        if (currentBufferSize <= len) {
            System.arraycopy(charBufferLocal, nowIndexLocal, cbuf, off, currentBufferSize);
            nowLimitLocal = nowIndexLocal = 0;
            this.setNowIndex(nowIndexLocal);
            this.setNowLimit(nowLimitLocal);
            return currentBufferSize;
        } else {
            System.arraycopy(charBufferLocal, nowIndexLocal, cbuf, off, len);
            nowIndexLocal += len;
            this.setNowIndex(nowIndexLocal);
            this.setNowLimit(nowLimitLocal);
            return len;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int peek() throws UncheckedIOException {
        final char[] charBufferLocal = this.getCharBuffer();
        final Reader readerLocal = this.getReader();
        int nowLimitLocal = this.getNowLimit();
        int nowIndexLocal = this.getNowIndex();

        final int currentBufferSize = nowLimitLocal - nowIndexLocal;
        if (currentBufferSize == 0) {
            nowLimitLocal = nowIndexLocal = 0;
            if (this.cachedCR) {
                charBufferLocal[nowLimitLocal++] = '\r';
            }

            int readLength;
            do {
                try {
                    readLength = readerLocal.read(charBufferLocal, 0, charBufferLocal.length - nowLimitLocal);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            } while (readLength == 0);
            if (readLength == EOF) {
                if (this.cachedCR) {
                    this.cachedCR = false;
                    this.setNowIndex(nowIndexLocal);
                    this.setNowLimit(nowLimitLocal);
                    return charBufferLocal[nowIndexLocal] = '\n';
                }
                this.setNowIndex(nowIndexLocal);
                this.setNowLimit(nowLimitLocal);
                return EOF;
            }
            nowLimitLocal += readLength;

            if (charBufferLocal[nowLimitLocal - 1] == '\r') {
                --nowLimitLocal;
                this.cachedCR = true;
            } else {
                this.cachedCR = false;
            }
            this.setNowLimit(nowLimitLocal);
            if (nowLimitLocal == 0) {
                this.setNowIndex(nowIndexLocal);
                return this.peek();
            }
            this.filter();
            nowLimitLocal = this.getNowLimit();
            nowIndexLocal = this.getNowIndex();
        }
        return charBufferLocal[nowIndexLocal];
    }

    /**
     * Make sure chars in the charBuffer have no '\r'.
     *   "\r\n" in original reader will become "\n",
     *   "\n" in original reader will become "\n",
     *   "\r" with normal character behind in original reader will become "\n".
     * Other chars should not change.
     * After the filter, change this.nowIndex accordingly.
     */
    private void filter() {
        final char[] charBufferLocal = this.getCharBuffer();

        int i = this.getNowLimit() - 1;
        int j = i;
        if (i >= 0) {
            for (; i >= 0; --i, --j) {
                if (charBufferLocal[i] == '\n') {
                    charBufferLocal[j] = '\n';
                    final int i_1 = i - 1;
                    if (i_1 >= 0 && charBufferLocal[i_1] == '\r') {
                        --i;
                    }
                } else if (charBufferLocal[i] == '\r') {
                    charBufferLocal[j] = '\n';
                } else {
                    charBufferLocal[j] = charBufferLocal[i];
                }
            }
            this.setNowIndex(j + 1);
        }
    }

    /**
     * getter for this.cacheCR
     * @return this.cacheCR
     */
    public boolean isCachedCR() {
        return this.cachedCR;
    }

    /**
     * setter for this.cacheCR
     * @param cachedCR this.cacheCR
     */
    public void setCachedCR(boolean cachedCR) {
        this.cachedCR = cachedCR;
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
    public int read() throws UncheckedIOException {
        final int res = this.peek();
        if (res != EOF) {
            eat();
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws UncheckedIOException {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
