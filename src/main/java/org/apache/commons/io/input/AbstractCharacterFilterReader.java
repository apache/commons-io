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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A filter reader that filters out characters where subclasses decide which characters to filter out.
 */
public abstract class AbstractCharacterFilterReader extends FilterReader {

    /**
     * Constructs a new reader.
     *
     * @param reader
     *            the reader to filter
     */
    protected AbstractCharacterFilterReader(final Reader reader) {
        super(reader);
    }

    @Override
    public int read() throws IOException {
        int ch;
        do {
            ch = in.read();
        } while (filter(ch));
        return ch;
    }

    /**
     * Returns true if the given character should be filtered out, false to keep the character.
     *
     * @param ch
     *            the character to test.
     * @return true if the given character should be filtered out, false to keep the character.
     */
    protected abstract boolean filter(int ch);

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final int read = super.read(cbuf, off, len);
        if (read == -1) {
            return -1;
        }
        int pos = off - 1;
        for (int readPos = off; readPos < off + read; readPos++) {
            if (filter(read)) {
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
