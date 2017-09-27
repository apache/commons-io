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
 * OutputStream implementation that writes the data to an {@link Appendable}
 * Object.
 * <p>
 * For example, can be used with any {@link java.io.Writer} or a {@link java.lang.StringBuilder}
 * or {@link java.lang.StringBuffer}.
 *
 * @since 2.5
 * @see java.lang.Appendable
 * @version $Id$
 */
public class AppendableOutputStream <T extends Appendable> extends OutputStream {

    private final T appendable;

    /**
     * Construct a new instance with the specified appendable.
     *
     * @param appendable the appendable to write to
     */
    public AppendableOutputStream(final T appendable) {
        this.appendable = appendable;
    }

    /**
     * Write a character to the underlying appendable.
     *
     * @param b the character to write
     * @throws IOException upon error
     */
    @Override
    public void write(final int b) throws IOException {
        appendable.append((char)b);
    }

    /**
     * Return the target appendable.
     *
     * @return the target appendable
     */
    public T getAppendable() {
        return appendable;
    }

}
