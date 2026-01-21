/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Reader that limits the number of characters read in a chunk of the specified size or less.
 */
public class ChunkedReader extends FilterReader {

    private final int chunkSize;

    public ChunkedReader(final Reader reader, final int chunkSize) {
        super(reader);
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        this.chunkSize = chunkSize;
    }

    @Override
    public void close() throws IOException {
        // nothing to do.
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return super.read(cbuf, off, len > chunkSize ? chunkSize : len);
    }

}