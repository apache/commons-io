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
import java.util.Objects;

import org.apache.commons.io.IOUtils;

/**
 *
 * An {@link InputStream} that repeats provided bytes for given target byte count.
 * <p>
 * Closing this input stream has no effect. The methods in this class can be called after the stream has been closed
 * without generating an {@link IOException}.
 * </p>
 *
 * @see InfiniteCircularInputStream
 * @since 2.8.0
 */
public class CircularInputStream extends InputStream {

    /**
     * Throws an {@link IllegalArgumentException} if the input contains -1.
     *
     * @param repeatContent input to validate.
     * @return the input.
     */
    private static byte[] validate(final byte[] repeatContent) {
        Objects.requireNonNull(repeatContent, "repeatContent");
        for (final byte b : repeatContent) {
            if (b == IOUtils.EOF) {
                throw new IllegalArgumentException("repeatContent contains the end-of-stream marker " + IOUtils.EOF);
            }
        }
        return repeatContent;
    }

    private long byteCount;
    private int position = -1;
    private final byte[] repeatedContent;
    private final long targetByteCount;

    /**
     * Creates an instance from the specified array of bytes.
     *
     * @param repeatContent Input buffer to be repeated this buffer is not copied.
     * @param targetByteCount How many bytes the read. A negative number means an infinite target count.
     */
    public CircularInputStream(final byte[] repeatContent, final long targetByteCount) {
        this.repeatedContent = validate(repeatContent);
        if (repeatContent.length == 0) {
            throw new IllegalArgumentException("repeatContent is empty.");
        }
        this.targetByteCount = targetByteCount;
    }

    @Override
    public int read() {
        if (targetByteCount >= 0) {
            if (byteCount == targetByteCount) {
                return IOUtils.EOF;
            }
            byteCount++;
        }
        position = (position + 1) % repeatedContent.length;
        return repeatedContent[position] & 0xff;
    }

}
