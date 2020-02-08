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
package org.apache.commons.io.testtools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Base class for testcases doing tests with files.
 */
public abstract class TestUtils {

    private TestUtils() {

    }

    public static void createFile(final File file, final long size)
            throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new java.io.FileOutputStream(file))) {
            generateTestData(output, size);
        }
    }

    public static byte[] generateTestData(final long size) {
        try {
            final ByteArrayOutputStream baout = new ByteArrayOutputStream();
            generateTestData(baout, size);
            return baout.toByteArray();
        } catch (final IOException ioe) {
            throw new RuntimeException("This should never happen: " + ioe.getMessage());
        }
    }

    public static void generateTestData(final OutputStream out, final long size)
            throws IOException {
        for (int i = 0; i < size; i++) {
            //output.write((byte)'X');

            // nice varied byte pattern compatible with Readers and Writers
            out.write((byte) ((i % 127) + 1));
        }
    }

    public static void createLineBasedFile(final File file, final String[] data) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            for (final String element : data) {
                output.println(element);
            }
        }
    }

    public static File newFile(final File testDirectory, final String filename) throws IOException {
        final File destination = new File(testDirectory, filename);
        /*
        assertTrue( filename + "Test output data file shouldn't previously exist",
                    !destination.exists() );
        */
        if (destination.exists()) {
            FileUtils.forceDelete(destination);
        }
        return destination;
    }

    public static void checkFile(final File file, final File referenceFile)
            throws Exception {
        assertTrue(file.exists(), "Check existence of output file");
        assertEqualContent(referenceFile, file);
    }

    /**
     * Assert that the content of two files is the same.
     */
    private static void assertEqualContent(final File f0, final File f1)
            throws IOException {
        /* This doesn't work because the filesize isn't updated until the file
         * is closed.
        assertTrue( "The files " + f0 + " and " + f1 +
                    " have differing file sizes (" + f0.length() +
                    " vs " + f1.length() + ")", ( f0.length() == f1.length() ) );
        */
        try (InputStream is0 = new FileInputStream(f0)) {
            try (InputStream is1 = new FileInputStream(f1)) {
                final byte[] buf0 = new byte[1024];
                final byte[] buf1 = new byte[1024];
                int n0 = 0;
                int n1;

                while (-1 != n0) {
                    n0 = is0.read(buf0);
                    n1 = is1.read(buf1);
                    assertTrue((n0 == n1),
                            "The files " + f0 + " and " + f1 +
                            " have differing number of bytes available (" + n0 + " vs " + n1 + ")");

                    assertTrue(Arrays.equals(buf0, buf1),
                            "The files " + f0 + " and " + f1 + " have different content");
                }
            }
        }
    }

    /**
     * Assert that the content of a file is equal to that in a byte[].
     *
     * @param b0   the expected contents
     * @param file the file to check
     * @throws IOException If an I/O error occurs while reading the file contents
     */
    public static void assertEqualContent(final byte[] b0, final File file) throws IOException {
        int count = 0, numRead = 0;
        final byte[] b1 = new byte[b0.length];
        try (InputStream is = new FileInputStream(file)) {
            while (count < b0.length && numRead >= 0) {
                numRead = is.read(b1, count, b0.length);
                count += numRead;
            }
            assertEquals(b0.length, count, "Different number of bytes: ");
            for (int i = 0; i < count; i++) {
                assertEquals(b0[i], b1[i], "byte " + i + " differs");
            }
        }
    }

    /**
     * Assert that the content of a file is equal to that in a char[].
     *
     * @param c0   the expected contents
     * @param file the file to check
     * @throws IOException If an I/O error occurs while reading the file contents
     */
    public static void assertEqualContent(final char[] c0, final File file) throws IOException {
        int count = 0, numRead = 0;
        final char[] c1 = new char[c0.length];
        try (Reader ir = new FileReader(file)) {
            while (count < c0.length && numRead >= 0) {
                numRead = ir.read(c1, count, c0.length);
                count += numRead;
            }
            assertEquals(c0.length, count, "Different number of chars: ");
            for (int i = 0; i < count; i++) {
                assertEquals(c0[i], c1[i], "char " + i + " differs");
            }
        }
    }

    public static void checkWrite(final OutputStream output) throws Exception {
        try {
            new java.io.PrintStream(output).write(0);
        } catch (final Throwable t) {
            fail("The copy() method closed the stream when it shouldn't have. " + t.getMessage());
        }
    }

    public static void checkWrite(final Writer output) throws Exception {
        try {
            new java.io.PrintWriter(output).write('a');
        } catch (final Throwable t) {
            fail("The copy() method closed the stream when it shouldn't have. " + t.getMessage());
        }
    }

    public static void deleteFile(final File file)
            throws Exception {
        if (file.exists()) {
            assertTrue(file.delete(), "Couldn't delete file: " + file);
        }
    }

    /**
     * Sleep for a guaranteed number of milliseconds unless interrupted.
     *
     * This method exists because Thread.sleep(100) can sleep for 0, 70, 100 or 200ms or anything else
     * it deems appropriate. Read the docs on Thread.sleep for further details.
     *
     * @param ms the number of milliseconds to sleep for
     * @throws InterruptedException if interrupted
     */
    public static void sleep(final long ms) throws InterruptedException {
        final long finishAt = System.currentTimeMillis() + ms;
        long remaining = ms;
        do {
            Thread.sleep(remaining);
            remaining = finishAt - System.currentTimeMillis();
        } while (remaining > 0);
    }

    public static void sleepQuietly(final long ms) {
        try {
            sleep(ms);
        } catch (final InterruptedException ignored){
        }
    }

}
