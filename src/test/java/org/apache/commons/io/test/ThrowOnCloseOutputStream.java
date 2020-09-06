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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.ProxyOutputStream;

/**
 * Helper class for checking behavior of IO classes.
 */
public class ThrowOnCloseOutputStream extends ProxyOutputStream {

    /**
     * Default ctor.
     */
    public ThrowOnCloseOutputStream() {
        super(NullOutputStream.NULL_OUTPUT_STREAM);
    }

    /**
     * @param proxy OutputStream to delegate to.
     */
    public ThrowOnCloseOutputStream(final OutputStream proxy) {
        super(proxy);
    }

    /** @see java.io.OutputStream#close() */
    @Override
    public void close() throws IOException {
        throw new IOException(getClass().getSimpleName() + ".close() called.");
    }

}
