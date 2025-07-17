/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A {@link Spliterator} for an {@link IOSpliterator} that throws {@link UncheckedIOException} instead of
 * {@link IOException}.
 * <p>
 * Keep package-private for now.
 * </p>
 *
 * @param <T> the type of elements returned by this iterator.
 */
final class UncheckedIOSpliterator<T> implements Spliterator<T> {

    private final IOSpliterator<T> delegate;

    UncheckedIOSpliterator(final IOSpliterator<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public int characteristics() {
        return delegate.characteristics();
    }

    @Override
    public long estimateSize() {
        return delegate.estimateSize();
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        Uncheck.accept(delegate::forEachRemaining, action::accept);
    }

    @Override
    public Comparator<? super T> getComparator() {
        return delegate.getComparator().asComparator();
    }

    @Override
    public long getExactSizeIfKnown() {
        return delegate.getExactSizeIfKnown();
    }

    @Override
    public boolean hasCharacteristics(final int characteristics) {
        return delegate.hasCharacteristics(characteristics);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        return Uncheck.apply(delegate::tryAdvance, action::accept);
    }

    @Override
    public Spliterator<T> trySplit() {
        return Uncheck.get(delegate::trySplit).unwrap();
    }

}
