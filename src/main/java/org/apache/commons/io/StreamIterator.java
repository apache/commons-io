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

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Wraps and presents a {@link Stream} as a {@link AutoCloseable} {@link Iterator} resource that automatically closes itself when reaching the end of stream.
 *
 * <h2>Warning</h2>
 * <p>
 * In order to close the stream, the call site MUST either close the stream it allocated OR call this iterator until the end.
 * </p>
 *
 * @param <E> The {@link Stream} and {@link Iterator} type.
 * @since 2.15.0
 */
public final class StreamIterator<E> implements Iterator<E>, AutoCloseable {

    /**
     * Wraps and presents a stream as a closable resource that automatically closes itself when reaching the end of stream.
     * <p>
     * <b>Warning</b>
     * </p>
     * <p>
     * In order to close the stream, the call site MUST either close the stream it allocated OR call this iterator until the end.
     * </p>
     *
     * @param <T>    The stream and iterator type.
     * @param stream The stream iterate.
     * @return A new iterator.
     */
    public static <T> StreamIterator<T> iterator(final Stream<T> stream) {
        return new StreamIterator<>(stream);
    }

    /**
     * The given stream's Iterator.
     */
    private final Iterator<E> iterator;

    /**
     * The given stream.
     */
    private final Stream<E> stream;

    /**
     * Whether {@link #close()} has been called.
     */
    private boolean closed;

    private StreamIterator(final Stream<E> stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.iterator = stream.iterator();
    }

    /**
     * Closes the underlying stream.
     */
    @Override
    public void close() {
        closed = true;
        stream.close();
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            // Calling Iterator#hasNext() on a closed java.nio.file.FileTreeIterator causes an IllegalStateException.
            return false;
        }
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

}
