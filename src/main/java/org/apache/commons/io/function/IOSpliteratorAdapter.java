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

import java.util.Objects;
import java.util.Spliterator;

/**
 * Adapts an {@link Spliterator} as an {@link IOSpliterator}.
 *
 * @param <T> the type of the stream elements.
 */
final class IOSpliteratorAdapter<T> implements IOSpliterator<T> {

    static <E> IOSpliteratorAdapter<E> adapt(final Spliterator<E> delegate) {
        return new IOSpliteratorAdapter<>(delegate);
    }

    private final Spliterator<T> delegate;

    IOSpliteratorAdapter(final Spliterator<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Spliterator<T> unwrap() {
        return delegate;
    }

}
