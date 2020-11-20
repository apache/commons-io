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

package org.apache.commons.io;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Wraps and presents a stream as a closable iterator resource that automatically closes itself when reaching the end
 * of stream.
 *
 * @param <E> The stream and iterator type.
 * @since 2.9.0
 */
class StreamIterator<E> implements Iterator<E>, Closeable {

    /**
     * Wraps and presents a stream as a closable resource that automatically closes itself when reaching the end of
     * stream.
     * <h2>Warning</h2>
     * <p>
     * In order to close the stream, the call site MUST either close the stream it allocated OR call the iterator until
     * the end.
     * </p>
     * 
     * @param <T> The stream and iterator type.
     * @param stream The stream iterate.
     * @return A new iterator.
     */
    @SuppressWarnings("resource") // Caller MUST close or iterate to the end.
    public static <T> Iterator<T> iterator(final Stream<T> stream) {
        return new StreamIterator<>(stream).iterator;
    }

    private StreamIterator(final Stream<E> stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.iterator = stream.iterator();
    }

    private final Iterator<E> iterator;
    private final Stream<E> stream;

    @Override
    public boolean hasNext() {
        final boolean hasNext = iterator.hasNext();
        if (!hasNext) {
            close();
        }
        return hasNext;
    }

    @Override
    public E next() {
        final E next = iterator.next();
        if (next == null) {
            close();
        }
        return next;
    }

    /**
     * Closes the underlying stream.
     */
    @Override
    public void close() {
        stream.close();

    }

}
