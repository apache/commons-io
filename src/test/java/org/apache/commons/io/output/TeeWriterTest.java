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

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit Test Case for {@link TeeWriter}.
 */
public class TeeWriterTest {

    private static class ExceptionOnCloseStringWriter extends StringWriter {

        @Override
        public void close() throws IOException {
            throw new IOException();
        }
    }

    private static class RecordCloseStringWriter extends StringWriter {

        boolean closed;

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    /**
     * Tests that the branch {@code Writer} is closed when closing the main {@code Writer} throws an
     * exception on {@link TeeWriter#close()}.
     */
    @Test
    public void testCloseBranchIOException() {
        final StringWriter badW = new ExceptionOnCloseStringWriter();
        final RecordCloseStringWriter goodW = new RecordCloseStringWriter();
        final TeeWriter tw = new TeeWriter(goodW, badW);
        try {
            tw.close();
            Assert.fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            Assert.assertTrue(goodW.closed);
        }
    }

    /**
     * Tests that the main {@code Writer} is closed when closing the branch {@code Writer} throws an
     * exception on {@link TeeWriter#close()}.
     */
    @Test
    public void testCloseMainIOException() {
        final StringWriter badW = new ExceptionOnCloseStringWriter();
        final RecordCloseStringWriter goodW = new RecordCloseStringWriter();
        final TeeWriter tw = new TeeWriter(badW, goodW);
        try {
            tw.close();
            Assert.fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            Assert.assertTrue(goodW.closed);
        }
    }

    @Test
    public void testTee() throws IOException {
        final StringBuilderWriter sbw1 = new StringBuilderWriter();
        final StringBuilderWriter sbw2 = new StringBuilderWriter();
        final StringBuilderWriter expected = new StringBuilderWriter();

        try (final TeeWriter tw = new TeeWriter(sbw1, sbw2)) {
            for (int i = 0; i < 20; i++) {
                tw.write(i);
                expected.write(i);
            }
            Assert.assertEquals("TeeWriter.write(int)", expected.toString(), sbw1.toString());
            Assert.assertEquals("TeeWriter.write(int)", expected.toString(), sbw2.toString());

            final char[] array = new char[10];
            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.write(array);
            expected.write(array);
            Assert.assertEquals("TeeWriter.write(char[])", expected.toString(), sbw1.toString());
            Assert.assertEquals("TeeWriter.write(char[])", expected.toString(), sbw2.toString());

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.write(array, 5, 5);
            expected.write(array, 5, 5);
            Assert.assertEquals("TeeOutputStream.write(byte[], int, int)", expected.toString(),
                    sbw1.toString());
            Assert.assertEquals("TeeOutputStream.write(byte[], int, int)", expected.toString(),
                    sbw2.toString());

            for (int i = 0; i < 20; i++) {
                tw.append((char) i);
                expected.append((char) i);
            }
            Assert.assertEquals("TeeWriter.append(char)", expected.toString(), sbw1.toString());
            Assert.assertEquals("TeeWriter.append(char)", expected.toString(), sbw2.toString());

            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.append(new String(array));
            expected.append(new String(array));
            Assert.assertEquals("TeeWriter.append(CharSequence)", expected.toString(), sbw1.toString());
            Assert.assertEquals("TeeWriter.write(CharSequence)", expected.toString(), sbw2.toString());

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.append(new String(array), 5, 5);
            expected.append(new String(array), 5, 5);
            Assert.assertEquals("TeeWriter.append(CharSequence, int, int)", expected.toString(),
                    sbw1.toString());
            Assert.assertEquals("TeeWriter.append(CharSequence, int, int)", expected.toString(),
                    sbw2.toString());

            expected.flush();
            expected.close();

            tw.flush();
        }
    }

}
