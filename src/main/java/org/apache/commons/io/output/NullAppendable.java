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

/**
 * Appends all data to the famous <b>/dev/null</b>.
 * <p>
 * This Appendable has no destination (file/socket etc.) and all characters written to it are ignored and lost.
 * </p>
 *
 * @since 2.8.0
 */
public class NullAppendable implements Appendable { // NOPMD Class will be final in 3.0.

    /**
     * A singleton.
     */
    public static final NullAppendable INSTANCE = new NullAppendable();

    /** Use the singleton. */
    private NullAppendable() {
        // no instances.
    }

    @Override
    public Appendable append(final char c) throws IOException {
        return this;
    }

    @Override
    public Appendable append(final CharSequence csq) throws IOException {
        return this;
    }

    @Override
    public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
        return this;
    }

}
