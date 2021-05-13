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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.function.IntPredicate;

/**
 * A filter reader that filters out characters where subclasses decide which characters to filter out.
 */
public abstract class AbstractCharacterFilterReader extends FilterReader {

    /**
     * Skips nothing.
     *
     * @since 2.9.0
     */
    protected static final IntPredicate SKIP_NONE = ch -> false;

    private final IntPredicate skip;

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter
     */
    protected AbstractCharacterFilterReader(final Reader reader) {
        this(reader, SKIP_NONE);
    }

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter.
     * @param skip Skip test.
     * @since 2.9.0
     */
    protected AbstractCharacterFilterReader(final Reader reader, final IntPredicate skip) {
        super(reader);
        this.skip = skip == null ? SKIP_NONE : skip;
    }

    /**
     * Returns true if the given character should be filtered out, false to keep the character.
     *
     * @param ch the character to test.
     * @return true if the given character should be filtered out, false to keep the character.
     */
    protected boolean filter(final int ch) {
        return skip.test(ch);
    }

    @Override
    public int read() throws IOException {
        int ch;
        do {
            ch = in.read();
        } while (ch != EOF && filter(ch));
        return ch;
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final int read = super.read(cbuf, off, len);
        if (read == EOF) {
            return EOF;
        }
        int pos = off - 1;
        for (int readPos = off; readPos < off + read; readPos++) {
            if (filter(cbuf[readPos])) {
                continue;
            }
            pos++;
            if (pos < readPos) {
                cbuf[pos] = cbuf[readPos];
            }
        }
        return pos - off + 1;
    }
}
