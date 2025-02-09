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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.input.ClosedInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the alternative ByteArrayOutputStream implementations.
 */
public class ByteArrayOutputStreamTest {

    private interface BAOSFactory<T extends AbstractByteArrayOutputStream<T>> {

        T newInstance();

        T newInstance(int size);
    }

    private static final class ByteArrayOutputStreamFactory implements BAOSFactory<ByteArrayOutputStream> {
        @Override
        public ByteArrayOutputStream newInstance() {
            return new ByteArrayOutputStream();
        }

        @Override
        public ByteArrayOutputStream newInstance(final int size) {
            return new ByteArrayOutputStream(size);
        }
    }

    private static final class UnsynchronizedByteArrayOutputStreamFactory implements BAOSFactory<UnsynchronizedByteArrayOutputStream> {
        @Override
        public UnsynchronizedByteArrayOutputStream newInstance() {
            return new UnsynchronizedByteArrayOutputStream();
        }

        @Override
        public UnsynchronizedByteArrayOutputStream newInstance(final int size) {
            return new UnsynchronizedByteArrayOutputStream(size);
        }
    }

    private static final byte[] ASCII_DATA;

    static {
        ASCII_DATA = new byte[64];
        for (byte i = 0; i < ASCII_DATA.length; i++) {
            ASCII_DATA[i] = (byte) (char) ('0' + i);
        }
    }

    private static Stream<Arguments> baosFactories() {
        return Stream.of(Arguments.of(ByteArrayOutputStream.class.getSimpleName(), new ByteArrayOutputStreamFactory()),
                Arguments.of(UnsynchronizedByteArrayOutputStream.class.getSimpleName(), new UnsynchronizedByteArrayOutputStreamFactory()));
    }

    private static boolean byteCmp(final byte[] src, final byte[] cmp) {
        for (int i = 0; i < cmp.length; i++) {
            if (src[i] != cmp[i]) {
                return false;
            }
        }
        return true;
    }

    private static Stream<Arguments> toBufferedInputStreamFunctionFactories() {
        final IOFunction<InputStream, InputStream> syncBaosToBufferedInputStream = ByteArrayOutputStream::toBufferedInputStream;
        final IOFunction<InputStream, InputStream> syncBaosToBufferedInputStreamWithSize = is -> ByteArrayOutputStream.toBufferedInputStream(is, 1024);
        final IOFunction<InputStream, InputStream> unSyncBaosToBufferedInputStream = UnsynchronizedByteArrayOutputStream::toBufferedInputStream;
        final IOFunction<InputStream, InputStream> unSyncBaosToBufferedInputStreamWithSize = is -> UnsynchronizedByteArrayOutputStream.toBufferedInputStream(is,
                1024);

        return Stream.of(Arguments.of("ByteArrayOutputStream.toBufferedInputStream(InputStream)", syncBaosToBufferedInputStream),
                Arguments.of("ByteArrayOutputStream.toBufferedInputStream(InputStream, int)", syncBaosToBufferedInputStreamWithSize),
                Arguments.of("UnsynchronizedByteArrayOutputStream.toBufferedInputStream(InputStream)", unSyncBaosToBufferedInputStream),
                Arguments.of("UnsynchronizedByteArrayOutputStream.toBufferedInputStream(InputStream, int)", unSyncBaosToBufferedInputStreamWithSize));
    }

    private void checkByteArrays(final byte[] expected, final byte[] actual) {
        if (expected.length != actual.length) {
            fail("Resulting byte arrays are not equally long");
        }
        if (!byteCmp(expected, actual)) {
            fail("Resulting byte arrays are not equal");
        }
    }

    private void checkStreams(final AbstractByteArrayOutputStream<?> actual, final java.io.ByteArrayOutputStream expected) {
        assertEquals(expected.size(), actual.size(), "Sizes are not equal");
        final byte[] buf = actual.toByteArray();
        final byte[] refbuf = expected.toByteArray();
        checkByteArrays(buf, refbuf);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidParameterizedConstruction(final String baosName, final BAOSFactory<?> baosFactory) {
        assertThrows(IllegalArgumentException.class, () -> baosFactory.newInstance(-1));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteLenUnder(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 0, -1));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetAndLenOver(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 0, 2));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetAndLenUnder(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(new byte[1], 1, -2));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetOver(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(IOUtils.EMPTY_BYTE_ARRAY, 1, 0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testInvalidWriteOffsetUnder(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            assertThrows(IndexOutOfBoundsException.class, () -> baout.write(null, -1, 0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toBufferedInputStreamFunctionFactories")
    public void testToBufferedInputStream(final String baosName, final IOFunction<InputStream, InputStream> toBufferedInputStreamFunction) throws IOException {
        final byte[] data = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

        try (ByteArrayInputStream bain = new ByteArrayInputStream(data)) {
            assertEquals(data.length, bain.available());

            try (InputStream buffered = toBufferedInputStreamFunction.apply(bain)) {
                assertEquals(data.length, buffered.available());

                assertArrayEquals(data, IOUtils.toByteArray(buffered));

            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toBufferedInputStreamFunctionFactories")
    public void testToBufferedInputStreamEmpty(final String baosName, final IOFunction<InputStream, InputStream> toBufferedInputStreamFunction)
            throws IOException {
        try (ByteArrayInputStream bain = new ByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY)) {
            assertEquals(0, bain.available());

            try (InputStream buffered = toBufferedInputStreamFunction.apply(bain)) {
                assertEquals(0, buffered.available());

            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testToInputStream(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance();
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // Write 8224 bytes
            writeByteArrayIndex(baout, ref, 32);
            for (int i = 0; i < 128; i++) {
                writeByteArrayIndex(baout, ref, 64);
            }

            // Get data before more writes
            try (InputStream in = baout.toInputStream()) {
                byte[] refData = ref.toByteArray();

                // Write some more data
                writeByteArrayIndex(baout, ref, new int[] { 2, 4, 8, 16 });

                // Check original data
                byte[] baoutData = IOUtils.toByteArray(in);
                assertEquals(8224, baoutData.length);
                checkByteArrays(refData, baoutData);

                // Check all data written
                try (InputStream in2 = baout.toInputStream()) {
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
    public void testToInputStreamEmpty(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance();
                // Get data before more writes
                InputStream in = baout.toInputStream()) {
            assertEquals(0, in.available());
            assertInstanceOf(ClosedInputStream.class, in);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testToInputStreamWithReset(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        // Make sure reset() do not destroy InputStream returned from toInputStream()
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance();
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // Write 8224 bytes
            writeByteArrayIndex(baout, ref, 32);
            for (int i = 0; i < 128; i++) {
                writeByteArrayIndex(baout, ref, 64);
            }

            // Get data before reset
            try (InputStream in = baout.toInputStream()) {
                byte[] refData = ref.toByteArray();

                // Reset and write some new data
                baout.reset();
                ref.reset();
                writeByteArrayIndex(baout, ref, new int[] { 2, 4, 8, 16 });

                // Check original data
                byte[] baoutData = IOUtils.toByteArray(in);
                assertEquals(8224, baoutData.length);
                checkByteArrays(refData, baoutData);

                // Check new data written after reset
                try (InputStream in2 = baout.toInputStream()) {
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
    public void testWriteByte(final String baosName, final BAOSFactory<?> baosFactory) throws Exception {
        int written;

        // The ByteArrayOutputStream is initialized with 32 bytes to match
        // the original more closely for this test.
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance(32);
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // First three writes
            written = writeByte(baout, ref, new int[] { 4, 10, 22 });
            assertEquals(36, written);
            checkStreams(baout, ref);

            // Another two writes to see if there are any bad effects after toByteArray()
            written = writeByte(baout, ref, new int[] { 20, 12 });
            assertEquals(32, written);
            checkStreams(baout, ref);

            // Now reset the streams
            baout.reset();
            ref.reset();

            // Test again to see if reset() had any bad effects
            written = writeByte(baout, ref, new int[] { 5, 47, 33, 60, 1, 0, 8 });
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Test the readFrom(InputStream) method
            baout.reset();
            written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
            // and vice-versa to test the writeTo() method.
            try (AbstractByteArrayOutputStream<?> baout1 = baosFactory.newInstance(32)) {
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
                try (AbstractByteArrayOutputStream<?> baos1 = baosFactory.newInstance();
                        AbstractByteArrayOutputStream<?> baos2 = baosFactory.newInstance()) {
                    assertSame(baos1.toByteArray(), baos2.toByteArray());
                }
            }
        }
    }

    // writeStringCharset

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testWriteByteArray(final String baosName, final BAOSFactory<?> baosFactory) throws Exception {
        int written;

        // The ByteArrayOutputStream is initialized with 32 bytes to match
        // the original more closely for this test.
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance(32);
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // First three writes
            written = writeByteArray(baout, ref, new int[] { 4, 10, 22 });
            assertEquals(36, written);
            checkStreams(baout, ref);

            // Another two writes to see if there are any bad effects after toByteArray()
            written = writeByteArray(baout, ref, new int[] { 20, 12 });
            assertEquals(32, written);
            checkStreams(baout, ref);

            // Now reset the streams
            baout.reset();
            ref.reset();

            // Test again to see if reset() had any bad effects
            written = writeByteArray(baout, ref, new int[] { 5, 47, 33, 60, 1, 0, 8 });
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Test the readFrom(InputStream) method
            baout.reset();
            written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
            // and vice-versa to test the writeTo() method.
            try (AbstractByteArrayOutputStream<?> baout1 = baosFactory.newInstance(32)) {
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
                try (AbstractByteArrayOutputStream<?> baos1 = baosFactory.newInstance();
                        AbstractByteArrayOutputStream<?> baos2 = baosFactory.newInstance()) {
                    assertSame(baos1.toByteArray(), baos2.toByteArray());
                }
            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testWriteByteArrayIndex(final String baosName, final BAOSFactory<?> baosFactory) throws Exception {
        int written;

        // The ByteArrayOutputStream is initialized with 32 bytes to match
        // the original more closely for this test.
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance(32);
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // First three writes
            written = writeByteArrayIndex(baout, ref, new int[] { 4, 10, 22 });
            assertEquals(36, written);
            checkStreams(baout, ref);

            // Another two writes to see if there are any bad effects after toByteArray()
            written = writeByteArrayIndex(baout, ref, new int[] { 20, 12 });
            assertEquals(32, written);
            checkStreams(baout, ref);

            // Now reset the streams
            baout.reset();
            ref.reset();

            // Test again to see if reset() had any bad effects
            written = writeByteArrayIndex(baout, ref, new int[] { 5, 47, 33, 60, 1, 0, 8 });
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Test the readFrom(InputStream) method
            baout.reset();
            written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
            // and vice-versa to test the writeTo() method.
            try (AbstractByteArrayOutputStream<?> baout1 = baosFactory.newInstance(32)) {
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
                try (AbstractByteArrayOutputStream<?> baos1 = baosFactory.newInstance();
                        AbstractByteArrayOutputStream<?> baos2 = baosFactory.newInstance()) {
                    assertSame(baos1.toByteArray(), baos2.toByteArray());
                }
            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testWriteStringCharset(final String baosName, final BAOSFactory<?> baosFactory) throws Exception {
        int written;

        // The ByteArrayOutputStream is initialized with 32 bytes to match
        // the original more closely for this test.
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance(32);
                java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream()) {

            // First three writes
            written = writeStringCharset(baout, ref, new int[] { 4, 10, 22 });
            assertEquals(36, written);
            checkStreams(baout, ref);

            // Another two writes to see if there are any bad effects after toByteArray()
            written = writeStringCharset(baout, ref, new int[] { 20, 12 });
            assertEquals(32, written);
            checkStreams(baout, ref);

            // Now reset the streams
            baout.reset();
            ref.reset();

            // Test again to see if reset() had any bad effects
            written = writeStringCharset(baout, ref, new int[] { 5, 47, 33, 60, 1, 0, 8 });
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Test the readFrom(InputStream) method
            baout.reset();
            written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
            assertEquals(155, written);
            checkStreams(baout, ref);

            // Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
            // and vice-versa to test the writeTo() method.
            try (AbstractByteArrayOutputStream<?> baout1 = baosFactory.newInstance(32)) {
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
                try (AbstractByteArrayOutputStream<?> baos1 = baosFactory.newInstance();
                        AbstractByteArrayOutputStream<?> baos2 = baosFactory.newInstance()) {
                    assertSame(baos1.toByteArray(), baos2.toByteArray());
                }
            }
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("baosFactories")
    public void testWriteZero(final String baosName, final BAOSFactory<?> baosFactory) throws IOException {
        try (AbstractByteArrayOutputStream<?> baout = baosFactory.newInstance()) {
            baout.write(IOUtils.EMPTY_BYTE_ARRAY, 0, 0);
            assertTrue(true, "Dummy");
        }
    }

    private int writeByte(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int count) {
        if (count > ASCII_DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            baout.write(100);
            ref.write(100);
            return 1;
        }
        for (int i = 0; i < count; i++) {
            baout.write(ASCII_DATA[i]);
            ref.write(ASCII_DATA[i]);
        }
        return count;
    }

    private int writeByte(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int[] instructions) throws IOException {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeByte(baout, ref, instruction);
        }
        return written;
    }

    private int writeByteArray(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int count) throws IOException {
        if (count > ASCII_DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            // length 1 data
            baout.write(new byte[] { 100 });
            ref.write(new byte[] { 100 });
            return 1;
        }
        baout.write(Arrays.copyOf(ASCII_DATA, count));
        ref.write(Arrays.copyOf(ASCII_DATA, count));
        return count;
    }

    private int writeByteArray(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int[] instructions)
            throws IOException {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeByteArray(baout, ref, instruction);
        }
        return written;
    }

    private int writeByteArrayIndex(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int count) {
        if (count > ASCII_DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            // length 1 data
            baout.write(100);
            ref.write(100);
            return 1;
        }
        baout.write(ASCII_DATA, 0, count);
        ref.write(ASCII_DATA, 0, count);
        return count;
    }

    private int writeByteArrayIndex(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int[] instructions) {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeByteArrayIndex(baout, ref, instruction);
        }
        return written;
    }

    private int writeStringCharset(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int count) throws IOException {
        if (count > ASCII_DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            // length 1 data
            final String data = "a";
            baout.write(data, StandardCharsets.UTF_8);
            ref.write(data.getBytes(StandardCharsets.UTF_8));
            return 1;
        }
        final String data = new String(ASCII_DATA, StandardCharsets.UTF_8).substring(0, count);
        assertEquals(count, data.length(), () -> String.format("[%,d]:'%s'", count, data));
        baout.write(data, StandardCharsets.UTF_8);
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        // For now, test assumes 1-1 size mapping from chars to bytes.
        assertEquals(count, bytes.length, () -> String.format("[%,d]:'%s'", count, data));
        ref.write(bytes);
        return count;
    }

    private int writeStringCharset(final AbstractByteArrayOutputStream<?> baout, final java.io.ByteArrayOutputStream ref, final int[] instructions)
            throws IOException {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeStringCharset(baout, ref, instruction);
        }
        return written;
    }
}
