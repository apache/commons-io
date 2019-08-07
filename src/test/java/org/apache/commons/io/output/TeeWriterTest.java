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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.testtools.YellOnCloseWriter;
import org.junit.Test;

/**
 * JUnit Test Case for {@link TeeWriter}.
 */
public class TeeWriterTest {

    /**
     * Tests that the branch {@code Writer} is closed when closing the main {@code Writer} throws an
     * exception on {@link TeeWriter#close()}.
     */
    @Test
    public void testCloseBranchIOException() throws IOException {
        final Writer badW = new YellOnCloseWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW);
        try {
            tw.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodW).close();
        }
    }

    /**
     * Tests that the main {@code Writer} is closed when closing the branch {@code Writer} throws an
     * exception on {@link TeeWriter#close()}.
     */
    @Test
    public void testCloseMainIOException() throws IOException {
        final Writer badW = new YellOnCloseWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW);
        try {
            tw.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodW).close();
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
            assertEquals("TeeWriter.write(int)", expected.toString(), sbw1.toString());
            assertEquals("TeeWriter.write(int)", expected.toString(), sbw2.toString());

            final char[] array = new char[10];
            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.write(array);
            expected.write(array);
            assertEquals("TeeWriter.write(char[])", expected.toString(), sbw1.toString());
            assertEquals("TeeWriter.write(char[])", expected.toString(), sbw2.toString());

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.write(array, 5, 5);
            expected.write(array, 5, 5);
            assertEquals("TeeOutputStream.write(byte[], int, int)", expected.toString(),
                    sbw1.toString());
            assertEquals("TeeOutputStream.write(byte[], int, int)", expected.toString(),
                    sbw2.toString());

            for (int i = 0; i < 20; i++) {
                tw.append((char) i);
                expected.append((char) i);
            }
            assertEquals("TeeWriter.append(char)", expected.toString(), sbw1.toString());
            assertEquals("TeeWriter.append(char)", expected.toString(), sbw2.toString());

            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.append(new String(array));
            expected.append(new String(array));
            assertEquals("TeeWriter.append(CharSequence)", expected.toString(), sbw1.toString());
            assertEquals("TeeWriter.write(CharSequence)", expected.toString(), sbw2.toString());

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.append(new String(array), 5, 5);
            expected.append(new String(array), 5, 5);
            assertEquals("TeeWriter.append(CharSequence, int, int)", expected.toString(),
                    sbw1.toString());
            assertEquals("TeeWriter.append(CharSequence, int, int)", expected.toString(),
                    sbw2.toString());

            expected.flush();
            expected.close();

            tw.flush();
        }
    }

}
