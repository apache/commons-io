/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that will end when the amount of bytes read reaches
 * the configured number of maxBytes.
 * This is useful for preventing OutOfMemoryExceptions when downloading
 * very large files in cases where getting partial content is acceptable.
 */
public class MaxBytesInputStream extends CountingInputStream {
    private final long maxBytes;

    public MaxBytesInputStream(InputStream is, long maxBytes) {
        super(is);
        this.maxBytes = maxBytes;
    }

    @Override
    public int read() throws IOException {
        if (getByteCount() < this.maxBytes) {
            return super.read();
        }
        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long remain = this.maxBytes - getByteCount();
        if (remain > 0) {
            if (remain < len) { // avoid reading past max
                return super.read(b, off, (int) remain);                
            } else {
                return super.read(b, off, len);
            }
        }
        return -1;
    }
}