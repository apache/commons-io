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

package org.apache.commons.io.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Works with {@link CharsetDecoder}.
 *
 * @since 2.12.0
 */
public final class CharsetDecoders {

    /**
     * Returns the given non-null CharsetDecoder or a new default CharsetDecoder.
     *
     * @param charsetDecoder The CharsetDecoder to test.
     * @return the given non-null CharsetDecoder or a new default CharsetDecoder.
     */
    public static CharsetDecoder toCharsetDecoder(final CharsetDecoder charsetDecoder) {
        return charsetDecoder != null ? charsetDecoder : Charset.defaultCharset().newDecoder();
    }

    /** No instances. */
    private CharsetDecoders() {
        // No instances.
    }
}
