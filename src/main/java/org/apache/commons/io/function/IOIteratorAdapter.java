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
import java.util.Iterator;
import java.util.Objects;

/**
 * Adapts an {@link Iterator} as an {@link IOIterator}.
 *
 * @param <E> the type of the stream elements.
 */
final class IOIteratorAdapter<E> implements IOIterator<E> {

    static <E> IOIteratorAdapter<E> adapt(final Iterator<E> delegate) {
        return new IOIteratorAdapter<>(delegate);
    }

    private final Iterator<E> delegate;

    IOIteratorAdapter(final Iterator<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean hasNext() throws IOException {
        return delegate.hasNext();
    }

    @Override
    public E next() throws IOException {
        return delegate.next();
    }

    @Override
    public Iterator<E> unwrap() {
        return delegate;
    }

}
