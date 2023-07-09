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

import java.io.Writer;

/**
 * Proxy writer that prevents the underlying writer from being closed.
 * <p>
 * This class is typically used in cases where a writer needs to be passed to a
 * component that wants to explicitly close the writer even if other components
 * would still use the writer for output.
 * </p>
 *
 * @since 2.7
 */
public class CloseShieldWriter extends ProxyWriter {

    /**
     * Constructs a proxy that shields the given writer from being closed.
     *
     * @param writer the writer to wrap
     * @return the created proxy
     * @since 2.9.0
     */
    public static CloseShieldWriter wrap(final Writer writer) {
        return new CloseShieldWriter(writer);
    }

    /**
     * Constructs a proxy that shields the given writer from being closed.
     *
     * @param writer underlying writer
     * @deprecated Using this constructor prevents IDEs from warning if the
     *             underlying writer is never closed. Use {@link #wrap(Writer)}
     *             instead.
     */
    @Deprecated
    public CloseShieldWriter(final Writer writer) {
        super(writer);
    }

    /**
     * Replaces the underlying writer with a {@link ClosedWriter} sentinel. The
     * original writer will remain open, but this proxy will appear closed.
     */
    @Override
    public void close() {
        out = ClosedWriter.INSTANCE;
    }

}
