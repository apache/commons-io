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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TeeWriter}.
 */
public class TeeWriterTest {

    @Test
    public void testArrayIOExceptionOnAppendChar1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final ProxyCollectionWriter tw = new ProxyCollectionWriter(badW, goodW, null);
        final char data = 'A';
        try {
            tw.append(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnAppendChar2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final ProxyCollectionWriter tw = new ProxyCollectionWriter(goodW, badW, null);
        final char data = 'A';
        try {
            tw.append(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnAppendCharSequence1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";
        try {
            tw.append(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnAppendCharSequence2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";
        try {
            tw.append(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnAppendCharSequenceIntInt1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";
        try {
            tw.append(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnAppendCharSequenceIntInt2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";
        try {
            tw.append(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).append(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnClose1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        try {
            tw.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).close();
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnClose2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        try {
            tw.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).close();
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnFlush1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        try {
            tw.flush();
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).flush();
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnFlush2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        try {
            tw.flush();
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).flush();
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteCharArray1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final char[] data = new char[] { 'a' };
        try {
            tw.write(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteCharArray2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final char[] data = new char[] { 'a' };
        try {
            tw.write(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteCharArrayIntInt1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final char[] data = new char[] { 'a' };
        try {
            tw.write(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteCharArrayIntInt2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final char[] data = new char[] { 'a' };
        try {
            tw.write(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteInt1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final int data = 32;
        try {
            tw.write(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteInt2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        try {
            tw.write(32);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(32);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());

        }
    }

    @Test
    public void testArrayIOExceptionOnWriteString1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";
        try {
            tw.write(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteString2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";
        try {
            tw.write(data);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());

        }
    }

    @Test
    public void testArrayIOExceptionOnWriteStringIntInt1() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(badW, goodW, null);
        final String data = "A";
        try {
            tw.write(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(0, e.getCause(0, IOIndexedException.class).getIndex());
        }
    }

    @Test
    public void testArrayIOExceptionOnWriteStringIntInt2() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(goodW, badW, null);
        final String data = "A";
        try {
            tw.write(data, 0, 0);
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).write(data, 0, 0);
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());

        }
    }

    @Test
    public void testCollectionCloseBranchIOException() throws IOException {
        final Writer badW = new BrokenWriter();
        final StringWriter goodW = mock(StringWriter.class);
        final TeeWriter tw = new TeeWriter(Arrays.asList(goodW, badW, null));
        try {
            tw.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOExceptionList e) {
            verify(goodW).close();
            assertEquals(1, e.getCauseList().size());
            assertEquals(1, e.getCause(0, IOIndexedException.class).getIndex());

        }
    }

    @Test
    public void testConstructorsNull() throws IOException {
        try (final TeeWriter teeWriter = new TeeWriter((Writer[]) null)) {
            // Call any method, should not throw
            teeWriter.append('a');
            teeWriter.flush();
        }
        try (final TeeWriter teeWriter = new TeeWriter((Collection<Writer>) null)) {
            // Call any method, should not throw
            teeWriter.append('a');
            teeWriter.flush();
        }
    }

    @Test
    public void testTee() throws IOException {
        final StringBuilderWriter sbw1 = new StringBuilderWriter();
        final StringBuilderWriter sbw2 = new StringBuilderWriter();
        final StringBuilderWriter expected = new StringBuilderWriter();

        try (final TeeWriter tw = new TeeWriter(sbw1, sbw2, null)) {
            for (int i = 0; i < 20; i++) {
                tw.write(i);
                expected.write(i);
            }
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.write(int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.write(int)");

            final char[] array = new char[10];
            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.write(array);
            expected.write(array);
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.write(char[])");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.write(char[])");

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.write(array, 5, 5);
            expected.write(array, 5, 5);
            assertEquals(expected.toString(), sbw1.toString(), "TeeOutputStream.write(byte[], int, int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeOutputStream.write(byte[], int, int)");

            for (int i = 0; i < 20; i++) {
                tw.append((char) i);
                expected.append((char) i);
            }
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(char)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(char)");

            for (int i = 20; i < 30; i++) {
                array[i - 20] = (char) i;
            }
            tw.append(new String(array));
            expected.append(new String(array));
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(CharSequence)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(CharSequence)");

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (char) i;
            }
            tw.append(new String(array), 5, 5);
            expected.append(new String(array), 5, 5);
            assertEquals(expected.toString(), sbw1.toString(), "TeeWriter.append(CharSequence, int, int)");
            assertEquals(expected.toString(), sbw2.toString(), "TeeWriter.append(CharSequence, int, int)");

            expected.flush();
            expected.close();

            tw.flush();
        }
    }

}
