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

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOUtils} close methods.
 */
public class IOUtilsCloseTest {

    /** An {@link AutoCloseable} whose {@code close()} always throws. */
    private static class ThrowingAutoCloseable implements AutoCloseable {

        @Override
        public void close() throws Exception {
            throw new Exception("Intentional AutoCloseable close exception");
        }
    }

    /** A {@link Closeable} whose {@code close()} always throws {@link IOException}. */
    private static class ThrowingCloseable implements Closeable {

        @Override
        public void close() throws IOException {
            throw new IOException("Intentional close exception");
        }
    }

    /** An {@link AutoCloseable} that tracks when it is closed. */
    private static class AutoCloseableReference implements AutoCloseable {

        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        boolean isClosed() {
            return closed;
        }
    }

    /** A {@link Closeable} that tracks when it is closed. */
    private static class CloseableReference implements Closeable {

        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        boolean isClosed() {
            return closed;
        }
    }

    @Test
    public void testCloseQuietlyAutoCloseable_closes() {
        final AutoCloseableReference closeable = new AutoCloseableReference();
        IOUtils.closeQuietly(closeable);
        assertTrue(closeable.isClosed(), "closeQuietly should close the AutoCloseable");
    }

    @Test
    public void testCloseQuietlyAutoCloseable_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((AutoCloseable) null));
    }

    @Test
    public void testCloseQuietlyAutoCloseable_swallowsException() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ThrowingAutoCloseable()));
    }
    // closeQuietly(Closeable, Consumer<Exception>)

    @Test
    public void testCloseQuietlyAutoCloseable_withConsumer_exceptionConsumed() {
        final AtomicReference<Exception> captured = new AtomicReference<>();
        IOUtils.closeQuietly(new ThrowingAutoCloseable(), captured::set);
        assertNotNull(captured.get(), "Consumer should have received the exception");
    }

    @Test
    public void testCloseQuietlyAutoCloseable_withConsumer_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((AutoCloseable) null, e -> {
        }));
    }

    @Test
    public void testCloseQuietlyAutoCloseable_withNullConsumer_swallowsException() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ThrowingAutoCloseable(), (java.util.function.Consumer<Exception>) null));
    }

    @Test
    public void testCloseQuietlyCloseable_closes() throws IOException {
        final CloseableReference closeable = new CloseableReference();
        IOUtils.closeQuietly(closeable);
        assertTrue(closeable.isClosed(), "closeQuietly should close the Closeable");
    }
    // closeQuietly(AutoCloseable)

    @Test
    public void testCloseQuietlyCloseable_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Closeable) null));
    }

    @Test
    public void testCloseQuietlyCloseable_swallowsException() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ThrowingCloseable()));
    }

    @Test
    public void testCloseQuietlyCloseable_withConsumer_closes() {
        final CloseableReference closeable = new CloseableReference();
        IOUtils.closeQuietly(closeable, e -> {
        });
        assertTrue(closeable.isClosed());
    }
    // closeQuietly(AutoCloseable, Consumer<Exception>)

    @Test
    public void testCloseQuietlyCloseable_withConsumer_exceptionConsumed() {
        final AtomicReference<Exception> captured = new AtomicReference<>();
        IOUtils.closeQuietly(new ThrowingCloseable(), captured::set);
        assertNotNull(captured.get(), "Consumer should have received the exception");
    }

    @Test
    public void testCloseQuietlyCloseable_withConsumer_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Closeable) null, e -> {
        }));
    }

    @Test
    public void testCloseQuietlyCloseable_withNullConsumer_swallowsException() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ThrowingCloseable(), (java.util.function.Consumer<Exception>) null));
    }

    @Test
    public void testCloseQuietlyInputStream_closes() {
        final AtomicBoolean closed = new AtomicBoolean();
        final InputStream in = new InputStream() {

            @Override
            public void close() {
                closed.set(true);
            }

            @Override
            public int read() {
                return -1;
            }
        };
        IOUtils.closeQuietly(in);
        assertTrue(closed.get());
    }

    @Test
    public void testCloseQuietlyInputStream_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((InputStream) null));
    }

    @Test
    public void testCloseQuietlyInputStream_swallowsException() {
        final InputStream in = new InputStream() {

            @Override
            public void close() throws IOException {
                throw new IOException("close failed");
            }

            @Override
            public int read() {
                return -1;
            }
        };
        assertDoesNotThrow(() -> IOUtils.closeQuietly(in));
    }

    @Test
    public void testCloseQuietlyIterable_closesAll() {
        final CloseableReference first = new CloseableReference();
        final CloseableReference second = new CloseableReference();
        IOUtils.closeQuietly(Arrays.asList(first, second));
        assertTrue(first.isClosed());
        assertTrue(second.isClosed());
    }

    @Test
    public void testCloseQuietlyIterable_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Iterable<Closeable>) null));
    }

    @Test
    public void testCloseQuietlyOutputStream_closes() {
        final AtomicBoolean closed = new AtomicBoolean();
        final OutputStream out = new OutputStream() {

            @Override
            public void close() {
                closed.set(true);
            }

            @Override
            public void write(final int b) {
            }
        };
        IOUtils.closeQuietly(out);
        assertTrue(closed.get());
    }

    @Test
    public void testCloseQuietlyOutputStream_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((OutputStream) null));
    }

    @Test
    public void testCloseQuietlyOutputStream_swallowsException() {
        final OutputStream out = new OutputStream() {

            @Override
            public void close() throws IOException {
                throw new IOException("close failed");
            }

            @Override
            public void write(final int b) {
            }
        };
        assertDoesNotThrow(() -> IOUtils.closeQuietly(out));
    }

    @Test
    public void testCloseQuietlyReader_closes() {
        final StringReader reader = new StringReader("data");
        assertDoesNotThrow(() -> IOUtils.closeQuietly(reader));
    }

    @Test
    public void testCloseQuietlyReader_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Reader) null));
    }

    @Test
    public void testCloseQuietlySelector_closes() throws IOException {
        final Selector selector = Selector.open();
        assertTrue(selector.isOpen());
        IOUtils.closeQuietly(selector);
        assertTrue(!selector.isOpen(), "Selector should be closed");
    }

    @Test
    public void testCloseQuietlySelector_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Selector) null));
    }

    @Test
    public void testCloseQuietlyServerSocket_closes() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(0);
        assertDoesNotThrow(() -> IOUtils.closeQuietly(serverSocket));
        assertTrue(serverSocket.isClosed());
    }

    @Test
    public void testCloseQuietlyServerSocket_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((ServerSocket) null));
    }

    @Test
    public void testCloseQuietlySocket_closes() throws IOException {
        final Socket socket = new Socket();
        assertDoesNotThrow(() -> IOUtils.closeQuietly(socket));
        assertTrue(socket.isClosed());
    }

    @Test
    public void testCloseQuietlySocket_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Socket) null));
    }

    @Test
    public void testCloseQuietlyStream_closesAll() {
        final CloseableReference first = new CloseableReference();
        final CloseableReference second = new CloseableReference();
        IOUtils.closeQuietly(Stream.of(first, second));
        assertTrue(first.isClosed());
        assertTrue(second.isClosed());
    }

    @Test
    public void testCloseQuietlyStream_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Stream<Closeable>) null));
    }

    @Test
    public void testCloseQuietlyVarargs_closesAll() {
        final CloseableReference first = new CloseableReference();
        final CloseableReference second = new CloseableReference();
        IOUtils.closeQuietly(first, second);
        assertTrue(first.isClosed(), "First closeable should be closed");
        assertTrue(second.isClosed(), "Second closeable should be closed");
    }

    @Test
    public void testCloseQuietlyVarargs_closesRemaining_whenOneThrows() {
        final CloseableReference second = new CloseableReference();
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ThrowingCloseable(), second));
        assertTrue(second.isClosed(), "Remaining closeables should still be closed");
    }

    @Test
    public void testCloseQuietlyVarargs_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Closeable[]) null));
    }

    @Test
    public void testCloseQuietlyVarargs_withNullElements() {
        final CloseableReference valid = new CloseableReference();
        assertDoesNotThrow(() -> IOUtils.closeQuietly(null, valid, null));
        assertTrue(valid.isClosed());
    }

    @Test
    public void testCloseQuietlyWriter_closes() {
        final StringWriter writer = new StringWriter();
        assertDoesNotThrow(() -> IOUtils.closeQuietly(writer));
    }

    @Test
    public void testCloseQuietlyWriter_null() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Writer) null));
    }
}
