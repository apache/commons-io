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
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Never writes data. Calls never go beyond this class.
 * <p>
 * This output stream has no destination and all bytes written to it are ignored.
 * </p>
 */
public class NullOutputStream extends OutputStream {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final NullOutputStream INSTANCE = new NullOutputStream();

    /**
     * The singleton instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public static final NullOutputStream NULL_OUTPUT_STREAM = INSTANCE;

    /**
     * Deprecated in favor of {@link #INSTANCE}.
     * <p>
     * TODO: Will be private in 3.0.
     * </p>
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public NullOutputStream() {
        // empty
    }

    /**
     * Does nothing.
     *
     * @param b The This method ignores this parameter.
     * @throws IOException This method never throws any exceptions.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        // noop
    }

    /**
     * Does nothing.
     *
     * @param b This method ignores this parameter.
     * @param off This method ignores this parameter.
     * @param len This method ignores this parameter.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        // noop
    }

    /**
     * Does nothing.
     *
     * @param b This method ignores this parameter.
     */
    @Override
    public void write(final int b) {
        // noop
    }

}
