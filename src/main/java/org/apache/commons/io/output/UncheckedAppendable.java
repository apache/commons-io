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
import java.io.UncheckedIOException;

/**
 * An {@link Appendable} that throws {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @see Appendable
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public interface UncheckedAppendable extends Appendable {

    /**
     * Constructs a new instance on the given Appendable.
     *
     * @param appendable The Appendable to uncheck.
     * @return a new instance.
     */
    static UncheckedAppendable on(final Appendable appendable) {
        return new UncheckedAppendableImpl(appendable);
    }

    /**
     * Appends per {@link Appendable#append(char)} but rethrows {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    UncheckedAppendable append(char c);

    /**
     * Appends per {@link Appendable#append(CharSequence)} but rethrows {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    UncheckedAppendable append(CharSequence csq);

    /**
     * Appends per {@link Appendable#append(CharSequence, int, int)} but rethrows {@link IOException} as
     * {@link UncheckedIOException}.
     */
    @Override
    UncheckedAppendable append(CharSequence csq, int start, int end);
}
