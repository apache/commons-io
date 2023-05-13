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

import java.io.PrintStream;

/**
 * Never prints data. Calls never go beyond this class.
 * <p>
 * This print stream has no destination (file/socket etc.) and all bytes written to it are ignored and lost.
 * </p>
 *
 * @since 2.7
 */
public class NullPrintStream extends PrintStream {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final NullPrintStream INSTANCE = new NullPrintStream();

    /**
     * The singleton instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public static final NullPrintStream NULL_PRINT_STREAM = INSTANCE;

    /**
     * Constructs an instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public NullPrintStream() {
        // Relies on the default charset which is OK since we are not actually writing.
        super(NullOutputStream.INSTANCE);
    }

}
