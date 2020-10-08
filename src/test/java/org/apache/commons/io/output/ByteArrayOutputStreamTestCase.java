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
package org.apache.commons.io.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.input.ClosedInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Basic unit tests for the alternative ByteArrayOutputStream implementations.
 */
public class ByteArrayOutputStreamTestCase {

    private static final byte[] DATA;

    static {
        DATA = new byte[64];
        for (byte i = 0; i < 64; i++) {
            DATA[i] = i;
        }
    }

    private int writeData(final AbstractByteArrayOutputStream baout,
                final java.io.ByteArrayOutputStream ref,
                final int count) {
        if (count > DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            baout.write(100);
            ref.write(100);
            return 1;
        }
        baout.write(DATA, 0, count);
        ref.write(DATA, 0, count);
        return count;
    }

    private int writeData(final AbstractByteArrayOutputStream baout,
                final java.io.ByteArrayOutputStream ref,
                final int[] instructions) {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeData(baout, ref, instruction);
        }
        return written;
    }

    private static boolean byteCmp(final byte[] src, final byte[] cmp) {
        for (int i = 0; i < cmp.length; i++) {
            if (src[i] != cmp[i]) {
                return false;
            }
        }
        return true;
    }

    private void checkByteArrays(final byte[] expected, final byte[] actual) {
        if (expected.length != actual.length) {
            fail("Resulting byte arrays are not equally long");
        }
        if (!byteCmp(expected, actual)) {
            fail("Resulting byte arrays are not equal");
        }
    }

    private void checkStreams(
            final AbstractByteArrayOutputStream actual,
            final java.io.ByteArrayOutputStream expected) {
        assertEquals(expected.size(), actual.size(), "Sizes are not equal");
        final byte[] buf = actual.toByteArray();
        final byte[] refbuf = expected.toByteArray();
        checkByteArrays(buf, refbuf);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testWriteZero(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            baout.write(new byte[0], 0, 0);
            assertTrue(true, "Dummy");
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetUnder(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(null, -1, 0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetOver(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[0], 1, 0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteLenUnder(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 0, -1));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetAndLenUnder(final String baosName, final BAOSFactory<?> baosFactory)
        throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 1, -2));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetAndLenOver(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 0, 2));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidParameterizedConstruction(final String baosName, final BAOSFactory<?> baosFactory) {
        assertThrows(IllegalArgumentException.class, () ->
                baosFactory.newInstance(-1)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testToInputStreamEmpty(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance();
            // Get data before more writes
            final InputStream in = baout.toInputStream()) {
            assertEquals(0, in.available());
            assertTrue(in instanceof ClosedInputStream);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toBufferedInputStreamFunctionFactories")
    public void testToBufferedInputStreamEmpty(final String baosName, final IOFunction<InputStream, InputStream> toBufferedInputStreamFunction) throws IOException {
        try (final ByteArrayInputStream bain = new ByteArrayInputStream(new byte[0])) {
            assertEquals(0, bain.available());

            try (final InputStream buffered = toBufferedInputStreamFunction.apply(bain)) {
                assertEquals(0, buffered.available());

            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toBufferedInputStreamFunctionFactories")
    public void testToBufferedInputStream(final String baosName, final IOFunction<InputStream, InputStream> toBufferedInputStreamFunction) throws IOException {
        final byte data[] = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};

        try (final ByteArrayInputStream bain = new ByteArrayInputStream(data)) {
            assertEquals(data.length, bain.available());

            try (final InputStream buffered = toBufferedInputStreamFunction.apply(bain)) {
                assertEquals(data.length, buffered.available());

                assertArrayEquals(data, IOUtils.toByteArray(buffered));

            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testToInputStream(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance();
            final java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // Write 8224 bytes
            writeData(baout, ref, 32);
            for (int i = 0; i < 128; i++) {
                writeData(baout, ref, 64);
            }

            // Get data before more writes
            try (final InputStream in = baout.toInputStream()) {
                byte refData[] = ref.toByteArray();

                // Write some more data
                writeData(baout, ref, new int[] {2, 4, 8, 16});

                // Check original data
                byte baoutData[] = IOUtils.toByteArray(in);
                assertEquals(8224, baoutData.length);
                checkByteArrays(refData, baoutData);

                // Check all data written
                try (final InputStream in2 = baout.toInputStream()) {
                    baoutData = IOUtils.toByteArray(in2);
                }
                refData = ref.toByteArray();
                assertEquals(8254, baoutData.length);
                checkByteArrays(refData, baoutData);
            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testToInputStreamWithReset(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        // Make sure reset() do not destroy InputStream returned from toInputStream()
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance();
            final java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // Write 8224 bytes
            writeData(baout, ref, 32);
            for (int i = 0; i < 128; i++) {
                writeData(baout, ref, 64);
            }

            // Get data before reset
            try (final InputStream in = baout.toInputStream()) {
                byte refData[] = ref.toByteArray();

                // Reset and write some new data
                baout.reset();
                ref.reset();
                writeData(baout, ref, new int[] {2, 4, 8, 16});

                // Check original data
                byte baoutData[] = IOUtils.toByteArray(in);
                assertEquals(8224, baoutData.length);
                checkByteArrays(refData, baoutData);

                // Check new data written after reset
                try (final InputStream in2 = baout.toInputStream()) {
                    baoutData = IOUtils.toByteArray(in2);
                }
                refData = ref.toByteArray();
                assertEquals(30, baoutData.length);
                checkByteArrays(refData, baoutData);
            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testStream(final String baosName, final BAOSFactory<?> baosFactory) throws Exception {
        int written;

        // The ByteArrayOutputStream is initialized with 32 bytes to match
        // the original more closely for this test.
        try (final AbstractByteArrayOutputStream baout = baosFactory.newInstance(32);
            final java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // First three writes
            written = writeData(baout, ref, new int[] {4, 10, 22});
            assertEquals(36, written);
            checkStreams(baout, ref);

            // Another two writes to see if there are any bad effects after toByteArray()
            written = writeData(baout, ref, new int[] {20, 12});
            assertEquals(32, written);
            checkStreams(baout, ref);

            // Now reset the streams
            baout.reset();
            ref.reset();

            // Test again to see if reset() had any bad effects
            written = writeData(baout, ref, new int[] {5, 47, 33, 60, 1, 0, 8});
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Test the readFrom(InputStream) method
            baout.reset();
            written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
            // and vice-versa to test the writeTo() method.
            try (final AbstractByteArrayOutputStream baout1 = baosFactory.newInstance(32)) {
                ref.writeTo(baout1);
                final java.io.ByteArrayOutputStream ref1 = new java.io.ByteArrayOutputStream();
                baout.writeTo(ref1);
                checkStreams(baout1, ref1);

                // Testing toString(String)
                final String baoutString = baout.toString("ASCII");
                final String refString = ref.toString("ASCII");
                assertEquals(refString, baoutString, "ASCII decoded String must be equal");

                // Make sure that empty ByteArrayOutputStreams really don't create garbage
                // on toByteArray()
                try (final AbstractByteArrayOutputStream baos1 = baosFactory.newInstance();
                    final AbstractByteArrayOutputStream baos2 = baosFactory.newInstance()) {
                    assertSame(baos1.toByteArray(), baos2.toByteArray());
                }
            }
        }
    }

    private static Stream<Arguments> baosFactories() {
        return Stream.of(
                Arguments.of(ByteArrayOutputStream.class.getSimpleName(), new ByteArrayOutputStreamFactory()),
                Arguments.of(UnsynchronizedByteArrayOutputStream.class.getSimpleName(), new UnsynchronizedByteArrayOutputStreamFactory())
        );
    }

    private static class ByteArrayOutputStreamFactory implements BAOSFactory<ByteArrayOutputStream> {
        @Override
        public ByteArrayOutputStream newInstance() {
            return new ByteArrayOutputStream();
        }

        @Override
        public ByteArrayOutputStream newInstance(final int size) {
            return new ByteArrayOutputStream(size);
        }
    }

    private static class UnsynchronizedByteArrayOutputStreamFactory implements BAOSFactory<UnsynchronizedByteArrayOutputStream> {
        @Override
        public UnsynchronizedByteArrayOutputStream newInstance() {
            return new UnsynchronizedByteArrayOutputStream();
        }

        @Override
        public UnsynchronizedByteArrayOutputStream newInstance(final int size) {
            return new UnsynchronizedByteArrayOutputStream(size);
        }
    }

    private interface BAOSFactory<T extends AbstractByteArrayOutputStream> {
        T newInstance();
        T newInstance(final int size);
    }

    private static Stream<Arguments> toBufferedInputStreamFunctionFactories() {
        final IOFunction<InputStream, InputStream> syncBaosToBufferedInputStream = ByteArrayOutputStream::toBufferedInputStream;
        final IOFunction<InputStream, InputStream> syncBaosToBufferedInputStreamWithSize = is -> ByteArrayOutputStream.toBufferedInputStream(is, 1024);
        final IOFunction<InputStream, InputStream> unSyncBaosToBufferedInputStream = UnsynchronizedByteArrayOutputStream::toBufferedInputStream;
        final IOFunction<InputStream, InputStream> unSyncBaosToBufferedInputStreamWithSize = is -> UnsynchronizedByteArrayOutputStream.toBufferedInputStream(is, 1024);

        return Stream.of(
            Arguments.of("ByteArrayOutputStream.toBufferedInputStream(InputStream)", syncBaosToBufferedInputStream),
            Arguments.of("ByteArrayOutputStream.toBufferedInputStream(InputStream, int)", syncBaosToBufferedInputStreamWithSize),
            Arguments.of("UnsynchronizedByteArrayOutputStream.toBufferedInputStream(InputStream)", unSyncBaosToBufferedInputStream),
            Arguments.of("UnsynchronizedByteArrayOutputStream.toBufferedInputStream(InputStream, int)", unSyncBaosToBufferedInputStreamWithSize)
        );
    }
}
