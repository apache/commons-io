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
package org.apache.commons.io.output;

import static org.apache.commons.io.output.ThresholdingOutputStreamTest.assertThresholdingInitialState;
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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@code DeferredFileOutputStream}. See also the superclass {@link ThresholdingOutputStream}.
 *
 * @see ThresholdingOutputStream
 */
class DeferredFileOutputStreamTest extends AbstractTempDirTest {

    private static void assertDeferredInitialState(final DeferredFileOutputStream out) {
        assertTrue(out.isInMemory());
    }

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

    void assertContentsEquals(final DeferredFileOutputStream out) throws IOException {
        try (InputStream is = out.toInputStream()) {
            assertArrayEquals(testBytes, IOUtils.toByteArray(is));
        }
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is therefore written to disk. The actual data
     * written to disk is verified, as is the file itself.
     *
     * @param initialBufferSize the initial buffer size.
     * @throws IOException on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testAboveThreshold(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testAboveThreshold", "dat").toFile();
        final int threshold = testBytes.length - 5;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertFalse(out.isInMemory());
            assertNull(out.getData());
            assertEquals(testFile.length(), out.getByteCount());
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
    void testAboveThresholdGetInputStream(final int initialBufferSize, final @TempDir Path tempDir) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testAboveThreshold", "dat").toFile();
        final int threshold = testBytes.length - 5;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertFalse(out.isInMemory());
            assertEquals(testFile.length(), out.getByteCount());
            assertContentsEquals(out);
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests the case where the amount of data is exactly the same as the threshold. The behavior should be the same as
     * that for the amount of data being below (not exceeding) the threshold.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testAtThreshold(final int initialBufferSize) throws IOException {
        final int threshold = testBytes.length;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .get()) {
            // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertTrue(out.isInMemory());
            assertEquals(testBytes.length, out.getByteCount());
            final byte[] resultBytes = out.getData();
            assertEquals(testBytes.length, resultBytes.length);
            assertArrayEquals(resultBytes, testBytes);
        }
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and is therefore confined to memory.
     *
     * @throws IOException Thrown on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testBelowThreshold(final int initialBufferSize) throws IOException {
        final int threshold = testBytes.length + 42;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .get()) {
            // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertTrue(out.isInMemory());
            assertEquals(testBytes.length, out.getByteCount());
            final byte[] resultBytes = out.getData();
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
    void testBelowThresholdGetInputStream(final int initialBufferSize) throws IOException {
        final int threshold = testBytes.length + 42;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .get()) {
                // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertTrue(out.isInMemory());
            assertEquals(testBytes.length, out.getByteCount());
            assertContentsEquals(out);
        }
    }

    /**
     * Tests that writing beyond the threshold throws a {@link NoSuchFileException} when the directory supplied to
     * {@link DeferredFileOutputStream.Builder#setDirectory(Path)} does not exist.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testSetDirectoryNonExistent() throws IOException {
        final Path nonExistentDir = tempDirPath.resolve("does-not-exist");
        final int threshold = testBytes.length - 5;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setPrefix("commons-io-test")
                .setSuffix(".out")
                .setDirectory(nonExistentDir)
                .get()) {
                // @formatter:on
            assertThrows(NoSuchFileException.class, () -> out.write(testBytes, 0, testBytes.length));
        }
    }

    /**
     * Tests specifying a temporary file and the threshold is reached.
     * Verifies the temporary file is deleted automatically when the stream is closed.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testTempFileAboveThreshold(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final int threshold = testBytes.length - 5;
        Path capturedPath = null;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory(tempDirFile)
                .setDirectory(tempDirPath.toFile())
                .get()) {
                // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            assertNull(out.getFile(), "Check File is null-A");
            assertNull(out.getPath(), "Check Path is null-A");
            out.write(testBytes, 0, testBytes.length);
            assertFalse(out.isInMemory());
            assertEquals(testBytes.length, out.getByteCount());
            assertNull(out.getData());
            assertNotNull(out.getFile(), "Check file not null");
            assertTrue(out.getFile().exists(), "Check file exists");
            assertTrue(out.getFile().getName().startsWith(prefix), "Check prefix");
            assertTrue(out.getFile().getName().endsWith(suffix), "Check suffix");
            assertEquals(tempDirPath, out.getPath().getParent(), "Check dir");
            verifyResultFile(out.getFile());
            capturedPath = out.getPath();
        } // close() called here; temp file should be deleted
        assertFalse(Files.exists(capturedPath), "Temp file should be deleted after close()");
    }

    /**
     * Tests specifying a temporary file with prefix only and the threshold is reached.
     * Verifies the temporary file is deleted automatically when the stream is closed.
     *
     * @throws IOException Thrown on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testTempFileAboveThresholdPrefixOnly(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = null;
        final int threshold = testBytes.length - 5;
        Path capturedPath = null;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory((File) null)
                .setDirectory((Path) null)
                .get()) {
                // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            assertNull(out.getFile(), "Check File is null-A");
            assertNull(out.getPath(), "Check Path is null-A");
            out.write(testBytes, 0, testBytes.length);
            assertFalse(out.isInMemory());
            assertNull(out.getData());
            assertEquals(testBytes.length, out.getByteCount());
            assertNotNull(out.getFile(), "Check file not null");
            assertTrue(out.getFile().exists(), "Check file exists");
            assertTrue(out.getFile().getName().startsWith(prefix), "Check prefix");
            assertTrue(out.getFile().getName().endsWith(".tmp"), "Check suffix"); // ".tmp" is default
            verifyResultFile(out.getFile());
            capturedPath = out.getPath();
        } // close() called here; temp file should be deleted automatically
        assertFalse(Files.exists(capturedPath), "Temp file should be deleted after close()");
    }

    /**
     * Tests specifying a temporary file and the threshold not reached.
     *
     * @throws IOException Thrown on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testTempFileBelowThreshold(final int initialBufferSize) throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final int threshold = testBytes.length + 42;
        try (DeferredFileOutputStream out = new DeferredFileOutputStream(threshold, initialBufferSize, prefix, suffix, tempDirFile)) {
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            assertNull(out.getFile(), "Check File is null-A");
            assertNull(out.getPath(), "Check Path is null-A");
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertTrue(out.isInMemory());
            assertEquals(testBytes.length, out.getByteCount());
            assertNull(out.getFile(), "Check file is null-B");
        }
    }

    /**
     * Tests that the temporary file created by {@link Files#createTempFile} in
     * {@link DeferredFileOutputStream#thresholdReached()} IS automatically deleted when the
     * {@link DeferredFileOutputStream} is closed.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testTempFileDeletedAfterClose() throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final int threshold = testBytes.length - 5;
        Path capturedPath = null;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory(tempDirPath)
                .get()) {
                // @formatter:on
            assertNull(out.getPath(), "Temp file should not exist before threshold is reached");
            // Write enough data to exceed the threshold, triggering thresholdReached() and Files.createTempFile()
            out.write(testBytes, 0, testBytes.length);
            assertFalse(out.isInMemory(), "Data should have been written to disk after threshold exceeded");
            // Capture the temp file path before closing
            capturedPath = out.getPath();
            assertNotNull(capturedPath, "Temp file path should be set after threshold is reached");
            assertTrue(Files.exists(capturedPath), "Temp file should exist after threshold is reached");
        } // close() called here; temp file should be deleted automatically
        assertFalse(Files.exists(capturedPath), "Temp file created by Files.createTempFile() in thresholdReached() must be deleted on close()");
    }

    /**
     * Tests that the temporary file IS deleted on close when {@link DeferredFileOutputStream.Builder#setDeleteTempFileOnClose(boolean)} is explicitly set to
     * {@code true}.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testTempFileDeletedAfterCloseWhenFlagTrue() throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final int threshold = testBytes.length - 5;
        Path capturedPath = null;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                // @formatter:off
                .setThreshold(threshold)
                .setPrefix(prefix)
                .setSuffix(suffix)
                .setDirectory(tempDirPath)
                .setDeleteTempFileOnClose(true)
                .get()) {
                // @formatter:on
            out.write(testBytes, 0, testBytes.length);
            assertFalse(out.isInMemory(), "Data should have been written to disk after threshold exceeded");
            capturedPath = out.getPath();
            assertTrue(Files.exists(capturedPath), "Temp file should exist before close()");
        } // close() called here; temp file should be deleted because deleteTempFileOnClose=true
        assertFalse(Files.exists(capturedPath), "Temp file must be deleted on close() when setDeleteTempFileOnClose(true)");
    }

    /**
     * Tests specifying a temporary file and the threshold is reached.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testTempFileError() throws Exception {
        final String prefix = null;
        final String suffix = ".out";
        assertThrows(NullPointerException.class, () -> new DeferredFileOutputStream(testBytes.length - 5, prefix, suffix, tempDirFile));
    }

    /**
     * Tests that the temporary file is NOT deleted on close when {@link DeferredFileOutputStream.Builder#setDeleteTempFileOnClose(boolean)} is set to
     * {@code false}. The caller is then responsible for deleting it.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testTempFileNotDeletedAfterCloseWhenFlagFalse() throws IOException {
        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final int threshold = testBytes.length - 5;
        Path capturedPath = null;
        try {
            try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                    // @formatter:off
                    .setThreshold(threshold)
                    .setPrefix(prefix)
                    .setSuffix(suffix)
                    .setDirectory(tempDirPath)
                    .setDeleteTempFileOnClose(false)
                    .get()) {
                    // @formatter:on
                out.write(testBytes, 0, testBytes.length);
                assertFalse(out.isInMemory(), "Data should have been written to disk after threshold exceeded");
                capturedPath = out.getPath();
                assertNotNull(capturedPath, "Temp file path should be set after threshold is reached");
                assertTrue(Files.exists(capturedPath), "Temp file should exist before close()");
            } // close() called here; temp file must NOT be deleted because deleteTempFileOnClose=false
            assertTrue(Files.exists(capturedPath), "Temp file must NOT be deleted on close() when setDeleteTempFileOnClose(false)");
        } finally {
            // Caller is responsible for cleanup when deleteTempFileOnClose=false
            PathUtils.deleteIfExists(capturedPath);
        }
    }

    /**
     * Tests the case where the threshold is negative, and therefore the data is always written to disk. The actual data
     * written to disk is verified, as is the file itself.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testThresholdNegative(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testThresholdNegative", "dat").toFile();
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
                .setThreshold(-1)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            assertThresholdingInitialState(out, 0, 0);
            assertDeferredInitialState(out);
            out.write(testBytes, 0, testBytes.length);
            out.close();
            assertFalse(out.isInMemory());
            assertNull(out.getData());
            assertEquals(testFile.length(), out.getByteCount());
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests the case where there are multiple writes beyond the threshold, to ensure that the
     * {@code thresholdReached()} method is only called once, as the threshold is crossed for the first time.
     *
     * @throws IOException Thrown on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testThresholdReached(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testThresholdReached", "dat").toFile();
        final int threshold = testBytes.length / 2;
        try (DeferredFileOutputStream out = DeferredFileOutputStream.builder()
            // @formatter:off
                .setThreshold(threshold)
                .setBufferSize(initialBufferSize)
                .setOutputFile(testFile)
                .get()) {
            // @formatter:on
            assertThresholdingInitialState(out, threshold, 0);
            assertDeferredInitialState(out);
            final int chunkSize = testBytes.length / 3;
            out.write(testBytes, 0, chunkSize);
            out.write(testBytes, chunkSize, chunkSize);
            out.write(testBytes, chunkSize * 2, testBytes.length - chunkSize * 2);
            out.close();
            assertFalse(out.isInMemory());
            assertNull(out.getData());
            assertEquals(testBytes.length, out.getByteCount());
            verifyResultFile(testFile);
        }
    }

    /**
     * Tests whether writeTo() properly writes large content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testWriteToLarge(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToFile", "dat").toFile();
        final int threshold = testBytes.length / 2;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                DeferredFileOutputStream dfos = DeferredFileOutputStream.builder().setThreshold(threshold).setOutputFile(testFile).get()) {
            assertThresholdingInitialState(dfos, threshold, 0);
            assertDeferredInitialState(dfos);
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
    void testWriteToLargeCtor(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToFile", "dat").toFile();
        final int threshold = testBytes.length / 2;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                DeferredFileOutputStream dfos = new DeferredFileOutputStream(threshold, testFile)) {
            assertThresholdingInitialState(dfos, threshold, 0);
            assertDeferredInitialState(dfos);
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
     *
     * @throws IOException Thrown on a test failure.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    void testWriteToSmall(final int initialBufferSize) throws IOException {
        final File testFile = Files.createTempFile(tempDirPath, "testWriteToMem", "dat").toFile();
        final int threshold = testBytes.length * 2;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
                DeferredFileOutputStream dfos = new DeferredFileOutputStream(threshold, initialBufferSize, testFile)) {
            assertThresholdingInitialState(dfos, threshold, 0);
            assertDeferredInitialState(dfos);
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
        try (InputStream fis = Files.newInputStream(testFile.toPath())) {
            assertEquals(testBytes.length, fis.available());
            final byte[] resultBytes = new byte[testBytes.length];
            assertEquals(testBytes.length, fis.read(resultBytes));
            assertArrayEquals(resultBytes, testBytes);
            assertEquals(-1, fis.read(resultBytes));
        }
    }
}
