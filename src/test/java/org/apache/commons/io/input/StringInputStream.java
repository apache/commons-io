/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.io.input;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * An {@link InputStream} on a String.
 *
 * @since 2.12.0
 */
public class StringInputStream extends ReaderInputStream {

    /**
     * Creates a new instance on a String.
     *
     * @param source The source string, MUST not be null.
     * @return A new instance.
     */
    public static StringInputStream on(final String source) {
        return new StringInputStream(source);
    }

    /**
     * Creates a new instance on the empty String.
     */
    public StringInputStream() {
        this("", Charset.defaultCharset());
    }

    /**
     * Creates a new instance on a String.
     *
     * @param source The source string, MUST not be null.
     */
    public StringInputStream(final String source) {
        this(source, Charset.defaultCharset());
    }

    /**
     * Creates a new instance on a String for a Charset.
     *
     * @param source The source string, MUST not be null.
     * @param charset The source charset, MUST not be null.
     */
    public StringInputStream(final String source, final Charset charset) {
        super(new StringReader(source), charset);
    }

    /**
     * Creates a new instance on a String and for a Charset.
     *
     * @param source The source string, MUST not be null.
     * @param charset The source charset, MUST not be null.
     */
    public StringInputStream(final String source, final String charset) {
        super(new StringReader(source), charset);
    }

}
