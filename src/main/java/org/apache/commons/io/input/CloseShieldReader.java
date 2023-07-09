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

import java.io.Reader;

/**
 * Proxy reader that prevents the underlying reader from being closed.
 * <p>
 * This class is typically used in cases where a reader needs to be passed to a
 * component that wants to explicitly close the reader even if more input would
 * still be available to other components.
 * </p>
 *
 * @since 2.7
 */
public class CloseShieldReader extends ProxyReader {

    /**
     * Constructs a proxy that shields the given reader from being closed.
     *
     * @param reader the reader to wrap
     * @return the created proxy
     * @since 2.9.0
     */
    public static CloseShieldReader wrap(final Reader reader) {
        return new CloseShieldReader(reader);
    }

    /**
     * Constructs a proxy that shields the given reader from being closed.
     *
     * @param reader underlying reader
     * @deprecated Using this constructor prevents IDEs from warning if the
     *             underlying reader is never closed. Use {@link #wrap(Reader)}
     *             instead.
     */
    @Deprecated
    public CloseShieldReader(final Reader reader) {
        super(reader);
    }

    /**
     * Replaces the underlying reader with a {@link ClosedReader} sentinel. The
     * original reader will remain open, but this proxy will appear closed.
     */
    @Override
    public void close() {
        in = ClosedReader.INSTANCE;
    }

}
