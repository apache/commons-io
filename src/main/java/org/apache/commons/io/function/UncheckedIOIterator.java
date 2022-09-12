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

package org.apache.commons.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Objects;

/**
 * An {@link Iterator} for a {@link IOIterator} that throws {@link UncheckedIOException} instead of {@link IOException}.
 *
 * Keep package-private for now.
 *
 * @param <E> the type of elements returned by this iterator.
 */
final class UncheckedIOIterator<E> implements Iterator<E> {

    private final IOIterator<E> delegate;

    /**
     * Constructs a new instance.
     *
     * @param delegate The delegate
     */
    UncheckedIOIterator(final IOIterator<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean hasNext() {
        return Uncheck.get(delegate::hasNext);
    }

    @Override
    public E next() {
        return Uncheck.get(delegate::next);
    }

    @Override
    public void remove() {
        Uncheck.run(delegate::remove);
    }
}
