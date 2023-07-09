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

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

/**
 * Like {@link BaseStream} but throws {@link IOException}.
 *
 * @param <T> the type of the stream elements.
 * @param <S> the type of the IO stream extending {@code IOBaseStream}.
 * @param <B> the type of the stream extending {@code BaseStream}.
 * @since 2.12.0
 */
public interface IOBaseStream<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>> extends Closeable {

    /**
     * Constructs a {@link BaseStream} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an {@link UncheckedIOException} {@link BaseStream}.
     */
    @SuppressWarnings("unchecked")
    default BaseStream<T, B> asBaseStream() {
        return new UncheckedIOBaseStream<>((S) this);
    }

    /**
     * Like {@link BaseStream#close()}.
     *
     * @see BaseStream#close()
     */
    @Override
    default void close() {
        unwrap().close();
    }

    /**
     * Like {@link BaseStream#isParallel()}.
     *
     * @return See {@link BaseStream#isParallel() delegate}.
     * @see BaseStream#isParallel()
     */
    @SuppressWarnings("resource") // for unwrap()
    default boolean isParallel() {
        return unwrap().isParallel();
    }

    /**
     * Like {@link BaseStream#iterator()}.
     *
     * @return See {@link BaseStream#iterator() delegate}.
     * @see BaseStream#iterator()
     */
    @SuppressWarnings("resource") // for unwrap()
    default IOIterator<T> iterator() {
        return IOIteratorAdapter.adapt(unwrap().iterator());
    }

    /**
     * Like {@link BaseStream#onClose(Runnable)}.
     *
     * @param closeHandler See {@link BaseStream#onClose(Runnable) delegate}.
     * @return See {@link BaseStream#onClose(Runnable) delegate}.
     * @throws IOException if an I/O error occurs.
     * @see BaseStream#onClose(Runnable)
     */
    @SuppressWarnings({"unused", "resource"}) // throws IOException, unwrap()
    default S onClose(final IORunnable closeHandler) throws IOException {
        return wrap(unwrap().onClose(() -> Erase.run(closeHandler)));
    }

    /**
     * Like {@link BaseStream#parallel()}.
     *
     * @return See {@link BaseStream#parallel() delegate}.
     * @see BaseStream#parallel()
     */
    @SuppressWarnings({"resource", "unchecked"}) // for unwrap(), this
    default S parallel() {
        return isParallel() ? (S) this : wrap(unwrap().parallel());
    }

    /**
     * Like {@link BaseStream#sequential()}.
     *
     * @return See {@link BaseStream#sequential() delegate}.
     * @see BaseStream#sequential()
     */
    @SuppressWarnings({"resource", "unchecked"}) // for unwrap(), this
    default S sequential() {
        return isParallel() ? wrap(unwrap().sequential()) : (S) this;
    }

    /**
     * Like {@link BaseStream#spliterator()}.
     *
     * @return See {@link BaseStream#spliterator() delegate}.
     * @see BaseStream#spliterator()
     */
    @SuppressWarnings("resource") // for unwrap()
    default IOSpliterator<T> spliterator() {
        return IOSpliteratorAdapter.adapt(unwrap().spliterator());
    }

    /**
     * Like {@link BaseStream#unordered()}.
     *
     * @return See {@link BaseStream#unordered() delegate}.
     * @see java.util.stream.BaseStream#unordered()
     */
    @SuppressWarnings("resource") // for unwrap()
    default S unordered() {
        return wrap(unwrap().unordered());
    }

    /**
     * Unwraps this instance and returns the underlying {@link Stream}.
     * <p>
     * Implementations may not have anything to unwrap and that behavior is undefined for now.
     * </p>
     *
     * @return the underlying stream.
     */
    B unwrap();

    /**
     * Wraps a {@link Stream}.
     *
     * @param delegate The delegate.
     * @return An IO stream.
     */
    S wrap(B delegate);

}
