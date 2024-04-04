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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.AbstractTempDirTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@code DeferredFileOutputStream}.
 */
public class DeferredFileOutputStreamTest extends AbstractTempDirTest {

    public static IntStream data() {
        return IntStream.of(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);
    }

    /**
     * The test data as a string (which is the simplest form).
     */
    private final String testString = "0123456789";

    /**
     * The test data as a byte array, derived from the string.
     */
    private final byte[] testBytes = testString.getBytes();

    /**
     * Tests the case where the amount of data exceeds the threshold, and is therefore written to disk. The actual data
     * written to disk is verified, as is the file itself.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAboveThreshold(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testAboveThreshold", "dat").toFile();
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
                .setThreshold(testBytes.length - 5)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertFalse(dfos.isInMemory());
            assertNull(dfos.getData());
            assertEquals(testFile.length(), dfos.getByteCount());
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is therefore written to disk. The actual data
     * written to disk is verified, as is the file itself.
     * Testing the getInputStream() method.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAboveThresholdGetInputStream(final int initialBufferSize, final @TempDir Path tempDir) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testAboveThreshold", "dat").toFile();
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
                .setThreshold(testBytes.length - 5)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertFalse(dfos.isInMemory());
            assertEquals(testFile.length(), dfos.getByteCount());
            try (InputStream is = dfos.toInputStream()) {
                assertArrayEquals(testBytes, IOUtils.toByteArray(is));
            }
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests the case where the amount of data is exactly the same as the threshold. The behavior should be the same as
     * that for the amount of data being below (i.e. not exceeding) the threshold.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAtThreshold(final int initialBufferSize) throws IOException {
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(testBytes.length)
                .setBufferSize(initialBufferSize)
                .get()) {
            // @formatter:on
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertTrue(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());
            final byte[] resultBytes = dfos.getData();
            assertEquals(testBytes.length, resultBytes.length);
            assertArrayEquals(resultBytes, testBytes);
        }
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and is therefore confined to memory.
     * @throws IOException
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testBelowThreshold(final int initialBufferSize) throws IOException {
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(testBytes.length + 42)
                .setBufferSize(initialBufferSize)
                .get()) {
            // @formatter:on
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertTrue(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());
            final byte[] resultBytes = dfos.getData();
            assertEquals(testBytes.length, resultBytes.length);
            assertArrayEquals(resultBytes, testBytes);
        }
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and is therefore confined to memory.
     * Testing the getInputStream() method.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testBelowThresholdGetInputStream(final int initialBufferSize) throws IOException {
        // @formatter:off
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
                .setThreshold(testBytes.length + 42)
                .setBufferSize(initialBufferSize)
                .get()) {
        // @formatter:on
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertTrue(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());
            try (InputStream is = dfos.toInputStream()) {
                assertArrayEquals(testBytes, IOUtils.toByteArray(is));
            }
        }
    }

    /**
     * Tests specifying a temporary file and the threshold is reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThreshold(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        // @formatter:off
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
                .setThreshold(testBytes.length - 5)
                .setBufferSize(initialBufferSize)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory(tempDirFile)
                .setDirectory(tempDirPath.toFile())
                .get()) {
        // @formatter:on
            assertNull(dfos.getFile(), "Check File is null-A");
            assertNull(dfos.getPath(), "Check Path is null-A");
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertFalse(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());
            assertNull(dfos.getData());
            assertNotNull(dfos.getFile(), "Check file not null");
            assertTrue(dfos.getFile().exists(), "Check file exists");
            assertTrue(dfos.getFile().getName().startsWith(prefix), "Check prefix");
            assertTrue(dfos.getFile().getName().endsWith(suffix), "Check suffix");
            assertEquals(tempDirPath, dfos.getPath().getParent(), "Check dir");
            verifyResultFile(dfos.getFile());
        }
    }

    /**
     * Tests specifying a temporary file and the threshold is reached.
     * @throws IOException
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThresholdPrefixOnly(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = null;
        try (final DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(testBytes.length - 5)
                .setBufferSize(initialBufferSize)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory((Path) null)
                .get()) {
            // @formatter:on
            try {
                assertNull(dfos.getFile(), "Check File is null-A");
                assertNull(dfos.getPath(), "Check Path is null-A");
                dfos.write(testBytes, 0, testBytes.length);
                dfos.close();
                assertFalse(dfos.isInMemory());
                assertNull(dfos.getData());
                assertEquals(testBytes.length, dfos.getByteCount());
                assertNotNull(dfos.getFile(), "Check file not null");
                assertTrue(dfos.getFile().exists(), "Check file exists");
                assertTrue(dfos.getFile().getName().startsWith(prefix), "Check prefix");
                assertTrue(dfos.getFile().getName().endsWith(".tmp"), "Check suffix"); // ".tmp" is default
                verifyResultFile(dfos.getFile());
            } finally {
                // Delete the temporary file.
                dfos.getFile().delete();
            }
        }
    }

    /**
     * Tests specifying a temporary file and the threshold not reached.
     * @throws IOException
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileBelowThreshold(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        try (final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize, prefix, suffix, tempDirFile)) {
            assertNull(dfos.getFile(), "Check File is null-A");
            assertNull(dfos.getPath(), "Check Path is null-A");
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertTrue(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());
            assertNull(dfos.getFile(), "Check file is null-B");
        }
    }

    /**
     * Tests specifying a temporary file and the threshold is reached.
     *
     * @throws Exception
     */
    @Test
    public void testTempFileError() throws Exception {
        final String prefix = null;
        final String suffix = ".out";
        assertThrows(NullPointerException.class, () -> new DeferredFileOutputStream(testBytes.length - 5, prefix, suffix, tempDirFile));
    }

    /**
     * Tests the case where the threshold is negative, and therefore the data is always written to disk. The actual data
     * written to disk is verified, as is the file itself.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testThresholdNegative(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testThresholdNegative", "dat").toFile();
        try (DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
                .setThreshold(-1)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
            assertFalse(dfos.isInMemory());
            assertNull(dfos.getData());
            assertEquals(testFile.length(), dfos.getByteCount());
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests the case where there are multiple writes beyond the threshold, to ensure that the
     * {@code thresholdReached()} method is only called once, as the threshold is crossed for the first time.
     * @throws IOException
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testThresholdReached(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testThresholdReached", "dat").toFile();
        try (final DeferredFileOutputStream dfos = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(testBytes.length /2)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            // @formatter:on
            final int chunkSize = testBytes.length / 3;
            dfos.write(testBytes, 0, chunkSize);
            dfos.write(testBytes, chunkSize, chunkSize);
            dfos.write(testBytes, chunkSize * 2, testBytes.length - chunkSize * 2);
            dfos.close();
            assertFalse(dfos.isInMemory());
            assertNull(dfos.getData());
            assertEquals(testBytes.length, dfos.getByteCount());
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests whether writeTo() properly writes large content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToLarge(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToFile", "dat").toFile();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                DeferredFileOutputStream dfos = DeferredFileOutputStream.builder().setThreshold(testBytes.length / 2).setOutputFile(testFile).get()) {
            dfos.write(testBytes);

            assertTrue(testFile.exists());
            assertFalse(dfos.isInMemory());
            assertEquals(testBytes.length, dfos.getByteCount());

            assertThrows(IOException.class, () -> dfos.writeTo(baos));

            dfos.close();
            dfos.writeTo(baos);
            final byte[] copiedBytes = baos.toByteArray();
            assertArrayEquals(testBytes, copiedBytes);
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests whether writeTo() properly writes large content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToLargeCtor(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToFile", "dat").toFile();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length / 2, testFile)) {
            dfos.write(testBytes);

            assertTrue(testFile.exists());
            assertFalse(dfos.isInMemory());

            assertThrows(IOException.class, () -> dfos.writeTo(baos));
            assertEquals(testBytes.length, dfos.getByteCount());

            dfos.close();
            dfos.writeTo(baos);
            final byte[] copiedBytes = baos.toByteArray();
            assertArrayEquals(testBytes, copiedBytes);
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests whether writeTo() properly writes small content.
     * @throws IOException
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToSmall(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToMem", "dat").toFile();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length * 2, initialBufferSize, testFile)) {
            dfos.write(testBytes);

            assertTrue(dfos.isInMemory());

            assertThrows(IOException.class, () -> dfos.writeTo(baos));
            assertEquals(testBytes.length, dfos.getByteCount());

            dfos.close();
            dfos.writeTo(baos);
            final byte[] copiedBytes = baos.toByteArray();
            assertArrayEquals(testBytes, copiedBytes);
        }
    }

    /**
     * Verifies that the specified file contains the same data as the original test data.
     *
     * @param testFile The file containing the test output.
     */
    private void verifyResultFile(final File testFile) throws IOException {
        try (final InputStream fis = Files.newInputStream(testFile.toPath())) {
            assertEquals(testBytes.length, fis.available());

            final byte[] resultBytes = new byte[testBytes.length];
            assertEquals(testBytes.length, fis.read(resultBytes));

            assertArrayEquals(resultBytes, testBytes);
            assertEquals(-1, fis.read(resultBytes));
        }
    }
}
