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
package org.apache.commons.io.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ProxyOutputStream;

/**
 * Helper class for checking behavior of IO classes.
 */
public class ThrowOnFlushAndCloseOutputStream extends ProxyOutputStream {

    private boolean throwOnFlush;
    private boolean throwOnClose;

    /**
     * @param proxy OutputStream to delegate to.
     * @param throwOnFlush True if flush() is forbidden
     * @param throwOnClose True if close() is forbidden
     */
    public ThrowOnFlushAndCloseOutputStream(final OutputStream proxy, final boolean throwOnFlush,
        final boolean throwOnClose) {
        super(proxy);
        this.throwOnFlush = throwOnFlush;
        this.throwOnClose = throwOnClose;
    }

    /** @see java.io.OutputStream#flush() */
    @Override
    public void flush() throws IOException {
        if (throwOnFlush) {
            fail(getClass().getSimpleName() + ".flush() called.");
        }
        super.flush();
    }

    /** @see java.io.OutputStream#close() */
    @Override
    public void close() throws IOException {
        if (throwOnClose) {
            fail(getClass().getSimpleName() + ".close() called.");
        }
        super.close();
    }

    public void off() {
        throwOnFlush = false;
        throwOnClose = false;
    }

}
