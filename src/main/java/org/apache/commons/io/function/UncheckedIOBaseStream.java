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
import java.util.Spliterator;
import java.util.stream.BaseStream;

/**
 * An {@link BaseStream} for a {@link IOBaseStream} that throws {@link UncheckedIOException} instead of
 * {@link IOException}.
 *
 * Keep package-private for now.
 *
 * @param <T> the type of the stream elements.
 * @param <S> the type of the IO stream extending {@code IOBaseStream}.
 * @param <B> the type of the stream extending {@code BaseStream}.
 */
final class UncheckedIOBaseStream<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>> implements BaseStream<T, B> {

    private final S delegate;

    UncheckedIOBaseStream(final S delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean isParallel() {
        return delegate.isParallel();
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator().asIterator();
    }

    @SuppressWarnings("resource")
    @Override
    public B onClose(final Runnable closeHandler) {
        return Uncheck.apply(delegate::onClose, () -> closeHandler.run()).unwrap();
    }

    @SuppressWarnings("resource")
    @Override
    public B parallel() {
        return delegate.parallel().unwrap();
    }

    @SuppressWarnings("resource")
    @Override
    public B sequential() {
        return delegate.sequential().unwrap();
    }

    @Override
    public Spliterator<T> spliterator() {
        return delegate.spliterator().unwrap();
    }

    @SuppressWarnings("resource")
    @Override
    public B unordered() {
        return delegate.unordered().unwrap();
    }

}
