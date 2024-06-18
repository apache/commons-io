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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an alternative to {@link ByteArrayInputStream}
 * which removes the synchronization overhead for non-concurrent
 * access; as such this class is not thread-safe.
 *
 * Proxy stream that prevents the underlying input stream from being marked/reset.
 * <p>
 * This class is typically used in cases where an input stream that supports
 * marking needs to be passed to a component that wants to explicitly mark
 * the stream, but it is not desirable to allow marking of the stream.
 * </p>
 *
 * @since 2.8.0
 */
public class MarkShieldInputStream extends ProxyInputStream {

    /**
     * Constructs a proxy that shields the given input stream from being
     * marked or rest.
     *
     * @param in underlying input stream
     */
    public MarkShieldInputStream(final InputStream in) {
        super(in);
    }

    @SuppressWarnings("sync-override")
    @Override
    public void mark(final int readLimit) {
        // no-op
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @SuppressWarnings("sync-override")
    @Override
    public void reset() throws IOException {
        throw UnsupportedOperationExceptions.reset();
    }
}
