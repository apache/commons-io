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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOBaseStream}.
 */
public class IOBaseStreamTest {

    /**
     * Implements IOBaseStream with generics.
     */
    private static class IOBaseStreamFixture<T, S extends IOBaseStreamFixture<T, S, B>, B extends BaseStream<T, B>> implements IOBaseStream<T, S, B> {

        private final B baseStream;

        private IOBaseStreamFixture(final B baseStream) {
            this.baseStream = baseStream;
        }

        @Override
        public B unwrap() {
            return baseStream;
        }

        @SuppressWarnings("unchecked") // We are this here
        @Override
        public S wrap(final B delegate) {
            return delegate == baseStream ? (S) this : (S) new IOBaseStreamFixture<T, S, B>(delegate);
        }

    }

    /**
     * Implements IOBaseStream with a concrete type.
     */
    private static final class IOBaseStreamPathFixture<B extends BaseStream<Path, B>> extends IOBaseStreamFixture<Path, IOBaseStreamPathFixture<B>, B> {

        private IOBaseStreamPathFixture(final B baseStream) {
            super(baseStream);
        }

        @Override
        public IOBaseStreamPathFixture<B> wrap(final B delegate) {
            return delegate == unwrap() ? this : new IOBaseStreamPathFixture<>(delegate);
        }

    }

    private static final class MyRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        MyRuntimeException(final String message) {
            super(message);
        }

    }

    /** Sanity check */
    private BaseStream<Path, ? extends BaseStream<Path, ?>> baseStream;

    /** Generic version */
    private IOBaseStreamFixture<Path, ? extends IOBaseStreamFixture<Path, ?, ?>, ?> ioBaseStream;

    /** Concrete version */
    private IOBaseStreamPathFixture<? extends BaseStream<Path, ?>> ioBaseStreamPath;

    /** Adapter version */
    private IOStream<Path> ioBaseStreamAdapter;

    @BeforeEach
    public void beforeEach() {
        baseStream = createStreamOfPaths();
        ioBaseStream = createIOBaseStream();
        ioBaseStreamPath = createIOBaseStreamPath();
        ioBaseStreamAdapter = createIOBaseStreamAdapter();
    }

    private IOBaseStreamFixture<Path, ?, Stream<Path>> createIOBaseStream() {
        return new IOBaseStreamFixture<>(createStreamOfPaths());
    }

    private IOStream<Path> createIOBaseStreamAdapter() {
        return IOStreamAdapter.adapt(createStreamOfPaths());
    }

    private IOBaseStreamPathFixture<Stream<Path>> createIOBaseStreamPath() {
        return new IOBaseStreamPathFixture<>(createStreamOfPaths());
    }

    private Stream<Path> createStreamOfPaths() {
        return Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B);
    }

    @Test
    @AfterEach
    public void testClose() {
        baseStream.close();
        ioBaseStream.close();
        ioBaseStreamPath.close();
        ioBaseStream.asBaseStream().close();
        ioBaseStreamPath.asBaseStream().close();
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testIsParallel() {
        assertFalse(baseStream.isParallel());
        assertFalse(ioBaseStream.isParallel());
        assertFalse(ioBaseStream.asBaseStream().isParallel());
        assertFalse(ioBaseStreamPath.asBaseStream().isParallel());
        assertFalse(ioBaseStreamPath.isParallel());
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testIteratorPathIO() throws IOException {
        final AtomicReference<Path> ref = new AtomicReference<>();
        ioBaseStream.iterator().forEachRemaining(e -> ref.set(e.toRealPath()));
        assertEquals(TestConstants.ABS_PATH_B.toRealPath(), ref.get());
        //
        ioBaseStreamPath.asBaseStream().iterator().forEachRemaining(e -> ref.set(e.getFileName()));
        assertEquals(TestConstants.ABS_PATH_B.getFileName(), ref.get());
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testIteratorSimple() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        ioBaseStreamPath.asBaseStream().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
    }

    @SuppressWarnings("resource")
    @Test
    public void testOnClose() {
        // Stream
        testOnClose(baseStream);
        testOnClose(ioBaseStream.asBaseStream());
        testOnClose(ioBaseStreamPath.asBaseStream());
    }

    @SuppressWarnings("resource")
    private <T, S extends BaseStream<T, S>> void testOnClose(final BaseStream<T, S> stream) {
        final AtomicReference<String> refA = new AtomicReference<>();
        final AtomicReference<String> refB = new AtomicReference<>();
        stream.onClose(() -> refA.set("A"));
        stream.onClose(() -> {
            throw new MyRuntimeException("B");
        });
        stream.onClose(() -> {
            throw new MyRuntimeException("C");
        });
        stream.onClose(() -> refB.set("D"));
        final MyRuntimeException e = assertThrows(MyRuntimeException.class, stream::close);
        assertEquals("A", refA.get());
        assertEquals("D", refB.get());
        assertEquals("B", e.getMessage());
        final Throwable[] suppressed = e.getSuppressed();
        assertNotNull(suppressed);
        assertEquals(1, suppressed.length);
        assertEquals("C", suppressed[0].getMessage());
    }

    @SuppressWarnings("resource")
    @Test
    public void testParallel() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.parallel().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.parallel().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        final BaseStream<Path, ?> parallel = ioBaseStreamPath.asBaseStream().parallel();
        parallel.iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
        assertTrue(parallel.isParallel());
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testParallelParallel() {
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStream()) {
            testParallelParallel(stream);
        }
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStreamPath()) {
            testParallelParallel(stream);
        }
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStream()) {
            testParallelParallel(stream);
        }
        try (IOBaseStreamFixture<Path, ?, Stream<Path>> stream = createIOBaseStream()) {
            testParallelParallel(stream.asBaseStream());
        }
    }

    @SuppressWarnings("resource")
    private void testParallelParallel(final BaseStream<?, ?> stream) {
        final BaseStream<?, ?> seq = stream.sequential();
        assertFalse(seq.isParallel());
        final BaseStream<?, ?> p1 = seq.parallel();
        assertTrue(p1.isParallel());
        final BaseStream<?, ?> p2 = p1.parallel();
        assertTrue(p1.isParallel());
        assertSame(p1, p2);
    }

    @SuppressWarnings("resource")
    private void testParallelParallel(final IOBaseStream<?, ?, ?> stream) {
        final IOBaseStream<?, ?, ?> seq = stream.sequential();
        assertFalse(seq.isParallel());
        final IOBaseStream<?, ?, ?> p1 = seq.parallel();
        assertTrue(p1.isParallel());
        final IOBaseStream<?, ?, ?> p2 = p1.parallel();
        assertTrue(p1.isParallel());
        assertSame(p1, p2);
    }

    @SuppressWarnings("resource")
    @Test
    public void testSequential() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.sequential().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.sequential().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        ioBaseStreamPath.asBaseStream().sequential().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testSequentialSequential() {
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStream()) {
            testSequentialSequential(stream);
        }
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStreamPath()) {
            testSequentialSequential(stream);
        }
        try (IOBaseStream<?, ?, ?> stream = createIOBaseStream()) {
            testSequentialSequential(stream.asBaseStream());
        }
    }

    @SuppressWarnings("resource")
    private void testSequentialSequential(final BaseStream<?, ?> stream) {
        final BaseStream<?, ?> p = stream.parallel();
        assertTrue(p.isParallel());
        final BaseStream<?, ?> seq1 = p.sequential();
        assertFalse(seq1.isParallel());
        final BaseStream<?, ?> seq2 = seq1.sequential();
        assertFalse(seq1.isParallel());
        assertSame(seq1, seq2);
    }

    @SuppressWarnings("resource")
    private void testSequentialSequential(final IOBaseStream<?, ?, ?> stream) {
        final IOBaseStream<?, ?, ?> p = stream.parallel();
        assertTrue(p.isParallel());
        final IOBaseStream<?, ?, ?> seq1 = p.sequential();
        assertFalse(seq1.isParallel());
        final IOBaseStream<?, ?, ?> seq2 = seq1.sequential();
        assertFalse(seq1.isParallel());
        assertSame(seq1, seq2);
    }

    @SuppressWarnings("resource") // @AfterEach
    @Test
    public void testSpliterator() {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.spliterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.spliterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        ioBaseStreamPath.asBaseStream().spliterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
    }

    @SuppressWarnings("resource")
    @Test
    public void testUnordered() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.unordered().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.unordered().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        ioBaseStreamPath.asBaseStream().unordered().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
    }

    @SuppressWarnings("resource")
    @Test
    public void testUnwrap() {
        final AtomicInteger ref = new AtomicInteger();
        baseStream.iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
        ioBaseStream.unwrap().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(4, ref.get());
        ioBaseStreamPath.asBaseStream().iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(6, ref.get());
    }

    @Test
    public void testWrap() {
        final Stream<Path> stream = createStreamOfPaths();
        @SuppressWarnings("resource")
        final IOStream<Path> wrap = ioBaseStreamAdapter.wrap(stream);
        assertNotNull(wrap);
        assertEquals(stream, wrap.unwrap());
    }

}
