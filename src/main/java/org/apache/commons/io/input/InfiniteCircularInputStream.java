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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * An {@link InputStream} that infinitely repeats the provided bytes.
 * <p>
 * Closing this input stream has no effect. The methods in this class can be called after the stream has been closed
 * without generating an {@link IOException}.
 * </p>
 *
 * @since 2.6
 */
public class InfiniteCircularInputStream extends CircularInputStream {

    /**
     * Creates an instance from the specified array of bytes.
     *
     * @param repeatContent Input buffer to be repeated this buffer is not copied.
     */
    public InfiniteCircularInputStream(final byte[] repeatContent) {
        // A negative number means an infinite target count.
        super(repeatContent, -1);
    }

}
