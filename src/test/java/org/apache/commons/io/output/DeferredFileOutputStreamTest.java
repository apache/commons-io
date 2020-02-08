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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for the <code>DeferredFileOutputStream</code> class.
 *
 */
public class DeferredFileOutputStreamTest {

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
     * Tests the case where the amount of data falls below the threshold, and
     * is therefore confined to memory.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testBelowThreshold(int initialBufferSize)
    {
        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        final byte[] resultBytes = dfos.getData();
        assertEquals(testBytes.length, resultBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data is exactly the same as the
     * threshold. The behavior should be the same as that for the amount of
     * data being below (i.e. not exceeding) the threshold.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAtThreshold(int initialBufferSize) {
        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length, initialBufferSize, null);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());

        final byte[] resultBytes = dfos.getData();
        assertEquals(testBytes.length, resultBytes.length);
        assertTrue(Arrays.equals(resultBytes, testBytes));
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is
     * therefore written to disk. The actual data written to disk is verified,
     * as is the file itself.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAboveThreshold(int initialBufferSize) {
        final File testFile = new File("testAboveThreshold.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize, testFile);
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }

    /**
     * Tests the case where there are multiple writes beyond the threshold, to
     * ensure that the <code>thresholdReached()</code> method is only called
     * once, as the threshold is crossed for the first time.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testThresholdReached(int initialBufferSize) {
        final File testFile = new File("testThresholdReached.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length / 2, initialBufferSize, testFile);
        final int chunkSize = testBytes.length / 3;

        try
        {
            dfos.write(testBytes, 0, chunkSize);
            dfos.write(testBytes, chunkSize, chunkSize);
            dfos.write(testBytes, chunkSize * 2,
                    testBytes.length - chunkSize * 2);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }


    /**
     * Test whether writeTo() properly writes small content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToSmall(int initialBufferSize){
        final File testFile = new File("testWriteToMem.dat");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length *2, initialBufferSize, testFile);
        try{
            dfos.write(testBytes);

            assertFalse(testFile.exists());
            assertTrue(dfos.isInMemory());

            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closing");
            } catch (final IOException ioe) {
                // ok, as expected
            }

            dfos.close();
            dfos.writeTo(baos);
        } catch (final IOException ioe) {
            fail("Unexpected IOException");
        }
        final byte[] copiedBytes  = baos.toByteArray();
        assertTrue(Arrays.equals(testBytes, copiedBytes));

        testFile.delete();
    }

    /**
     * Test whether writeTo() properly writes large content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToLarge(int initialBufferSize){
        final File testFile = new File("testWriteToFile.dat");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length / 2, testFile);
        try{
            dfos.write(testBytes);

            assertTrue(testFile.exists());
            assertFalse(dfos.isInMemory());

            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closeing");
            } catch (final IOException ioe) {
                // ok, as expected
            }

            dfos.close();
            dfos.writeTo(baos);
        } catch (final IOException ioe) {
            fail("Unexpected IOException");
        }
        final byte[] copiedBytes  = baos.toByteArray();
        assertTrue(Arrays.equals(testBytes, copiedBytes));
        verifyResultFile(testFile);
        testFile.delete();
    }

    /**
     * Test specifying a temporary file and the threshold not reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileBelowThreshold(int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final File tempDir  = new File(".");
        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize, prefix, suffix, tempDir);
        assertNull(dfos.getFile(), "Check file is null-A");
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertTrue(dfos.isInMemory());
        assertNull(dfos.getFile(), "Check file is null-B");
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThreshold(int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final File tempDir  = new File(".");
        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize, prefix, suffix, tempDir);
        assertNull(dfos.getFile(), "Check file is null-A");
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());
        assertNotNull(dfos.getFile(), "Check file not null");
        assertTrue(dfos.getFile().exists(), "Check file exists");
        assertTrue(dfos.getFile().getName().startsWith(prefix), "Check prefix");
        assertTrue(dfos.getFile().getName().endsWith(suffix), "Check suffix");
        assertEquals(tempDir.getPath(), dfos.getFile().getParent(), "Check dir");

        verifyResultFile(dfos.getFile());

        // Delete the temporary file.
        dfos.getFile().delete();
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThresholdPrefixOnly(int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = null;
        final File tempDir  = null;
        final DeferredFileOutputStream dfos =
                new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize, prefix, suffix, tempDir);
        assertNull(dfos.getFile(), "Check file is null-A");
        try
        {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertFalse(dfos.isInMemory());
        assertNull(dfos.getData());
        assertNotNull(dfos.getFile(), "Check file not null");
        assertTrue(dfos.getFile().exists(), "Check file exists");
        assertTrue(dfos.getFile().getName().startsWith(prefix), "Check prefix");
        assertTrue(dfos.getFile().getName().endsWith(".tmp"), "Check suffix"); // ".tmp" is default

        verifyResultFile(dfos.getFile());

        // Delete the temporary file.
        dfos.getFile().delete();
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     * @throws Exception
     */
    @Test
    public void testTempFileError() throws Exception {

        final String prefix = null;
        final String suffix = ".out";
        final File tempDir  = new File(".");
        try
        {
            (new DeferredFileOutputStream(testBytes.length - 5, prefix, suffix, tempDir)).close();
            fail("Expected IllegalArgumentException ");
        }
        catch (final IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Verifies that the specified file contains the same data as the original
     * test data.
     *
     * @param testFile The file containing the test output.
     */
    private void verifyResultFile(final File testFile) {
        try
        {
            final FileInputStream fis = new FileInputStream(testFile);
            assertEquals(testBytes.length, fis.available());

            final byte[] resultBytes = new byte[testBytes.length];
            assertEquals(testBytes.length, fis.read(resultBytes));

            assertTrue(Arrays.equals(resultBytes, testBytes));
            assertEquals(-1, fis.read(resultBytes));

            try
            {
                fis.close();
            }
            catch (final IOException e) {
                // Ignore an exception on close
            }
        }
        catch (final FileNotFoundException e) {
            fail("Unexpected FileNotFoundException");
        }
        catch (final IOException e) {
            fail("Unexpected IOException");
        }
    }
}
