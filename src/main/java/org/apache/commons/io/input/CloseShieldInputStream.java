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

import java.io.InputStream;

/**
 * Proxy stream that prevents the underlying input stream from being closed.
 * <p>
 * This class is typically used in cases where an input stream needs to be
 * passed to a component that wants to explicitly close the stream even if more
 * input would still be available to other components.
 * </p>
 *
 * @since 1.4
 */
public class CloseShieldInputStream extends ProxyInputStream {

    /**
     * Creates a proxy that shields the given input stream from being closed.
     *
     * @param inputStream the input stream to wrap
     * @return the created proxy
     * @since 2.9.0
     */
    public static CloseShieldInputStream wrap(final InputStream inputStream) {
        return new CloseShieldInputStream(inputStream);
    }

    /**
     * Creates a proxy that shields the given input stream from being closed.
     *
     * @param inputStream underlying input stream
     * @deprecated Using this constructor prevents IDEs from warning if the
     *             underlying input stream is never closed. Use
     *             {@link #wrap(InputStream)} instead.
     */
    @Deprecated
    public CloseShieldInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Replaces the underlying input stream with a {@link ClosedInputStream}
     * sentinel. The original input stream will remain open, but this proxy will
     * appear closed.
     */
    @Override
    public void close() {
        in = ClosedInputStream.CLOSED_INPUT_STREAM;
    }

}
