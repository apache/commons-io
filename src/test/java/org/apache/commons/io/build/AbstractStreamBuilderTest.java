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

package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link AbstractStreamBuilder}.
 */
class AbstractStreamBuilderTest {

    public static class Builder extends AbstractStreamBuilder<char[], Builder> {

        @Override
        public char[] get() {
            final char[] arr = new char[getBufferSize()];
            Arrays.fill(arr, 'a');
            return arr;
        }

    }

    private static Stream<IOConsumer<Builder>> fileBasedConfigurers() throws URISyntaxException {
        final URI uri = Objects.requireNonNull(AbstractStreamBuilderTest.class.getResource(AbstractOriginTest.FILE_RES_RO)).toURI();
        final Path path = Paths.get(AbstractOriginTest.FILE_NAME_RO);
        // @formatter:off
        return Stream.of(
                b -> b.setByteArray(ArrayUtils.EMPTY_BYTE_ARRAY),
                b -> b.setFile(AbstractOriginTest.FILE_NAME_RO),
                b -> b.setFile(path.toFile()),
                b -> b.setPath(AbstractOriginTest.FILE_NAME_RO),
                b -> b.setPath(path),
                b -> b.setRandomAccessFile(new RandomAccessFile(AbstractOriginTest.FILE_NAME_RO, "r")),
                // We can convert FileInputStream to ReadableByteChannel, but not the reverse.
                // Therefore, we don't use Files.newInputStream.
                b -> b.setInputStream(new FileInputStream(AbstractOriginTest.FILE_NAME_RO)),
                b -> b.setChannel(Files.newByteChannel(path)),
                b -> b.setURI(uri));
        // @formatter:on
    }

    private void assertResult(final char[] arr, final int size) {
        assertNotNull(arr);
        assertEquals(size, arr.length);
        for (final char c : arr) {
            assertEquals('a', c);
        }
    }

    protected Builder builder() {
        return new Builder();
    }

    /**
     * Tests various ways to obtain a {@link SeekableByteChannel}.
     *
     * @param configurer Lambda to configure the builder.
     */
    @ParameterizedTest
    @MethodSource("fileBasedConfigurers")
    void getGetSeekableByteChannel(final IOConsumer<Builder> configurer) throws Exception {
        final Builder builder = builder();
        configurer.accept(builder);
        try (ReadableByteChannel channel = assertDoesNotThrow(() -> builder.getChannel(SeekableByteChannel.class))) {
            assertTrue(channel.isOpen());
        }
    }

    @Test
    void testBufferSizeChecker() {
        // sanity
        final Builder builder = builder();
        assertResult(builder.get(), builder.getBufferSize());
        // basic failure
        assertThrows(IllegalArgumentException.class, () -> builder().setBufferSizeMax(2).setBufferSize(3));
        // reset
        assertResult(builder.setBufferSizeMax(2).setBufferSizeMax(0).setBufferSize(3).get(), 3);
        // resize
        assertResult(builder().setBufferSizeMax(2).setBufferSizeChecker(i -> 100).setBufferSize(3).get(), 100);
    }

    /**
     * Tests various ways to obtain a byte array.
     *
     * @param configurer configures a builder.
     */
    @ParameterizedTest
    @MethodSource("fileBasedConfigurers")
    void testGetByteArray(final IOConsumer<Builder> configurer) throws Exception {
        final Builder builder = builder();
        configurer.accept(builder);
        assertNotNull(builder.getByteArray());
    }

    /**
     * Tests various ways to obtain a {@link InputStream}.
     *
     * @param configurer configures a builder.
     */
    @ParameterizedTest
    @MethodSource("fileBasedConfigurers")
    void testGetInputStream(final IOConsumer<Builder> configurer) throws Exception {
        final Builder builder = builder();
        configurer.accept(builder);
        try (InputStream inputStream = builder.getInputStream()) {
            assertNotNull(inputStream);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 4 })
    void testSetByteArrayGetByteArray(final int size) throws Exception {
        final Builder builder = builder();
        final byte[] randomBytes = RandomUtils.insecure().randomBytes(size);
        builder.setByteArray(randomBytes);
        assertArrayEquals(randomBytes, builder.getByteArray());
    }

    @Test
    void testSetFileGetByteArray() throws Exception {
        final Builder builder = builder();
        final Path path = Paths.get(AbstractOriginTest.FILE_NAME_RO);
        builder.setFile(path.toFile());
        assertArrayEquals(Files.readAllBytes(path), builder.getByteArray());
    }

    @Test
    void testSetOpenOptions() {
        final Builder builder = builder();
        assertEquals(0, builder.setOpenOptions().getOpenOptions().length);
        assertEquals(0, builder.setOpenOptions((OpenOption[]) null).getOpenOptions().length);
        assertEquals(1, builder.setOpenOptions(StandardOpenOption.READ).getOpenOptions().length);
        final OpenOption[] options = { StandardOpenOption.READ, StandardOpenOption.WRITE };
        assertArrayEquals(options, builder.setOpenOptions(options).getOpenOptions());
        // Check that the builder makes a defensive copy of the array.
        options[0] = null;
        options[1] = null;
        assertEquals(StandardOpenOption.READ, builder.getOpenOptions()[0]);
        assertEquals(StandardOpenOption.WRITE, builder.getOpenOptions()[1]);
    }

    @Test
    void testSetPathGetByteArray() throws Exception {
        final Builder builder = builder();
        final Path path = Paths.get(AbstractOriginTest.FILE_NAME_RO);
        builder.setPath(path);
        assertArrayEquals(Files.readAllBytes(path), builder.getByteArray());
    }
}
