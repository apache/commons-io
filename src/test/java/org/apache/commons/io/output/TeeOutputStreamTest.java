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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.testtools.YellOnCloseOutputStream;
import org.junit.jupiter.api.Test;

/**On
 * JUnit Test Case for {@link TeeOutputStream}.
 */
public class TeeOutputStreamTest {

    /**
     * Tests that the branch {@code OutputStream} is closed when closing the main {@code OutputStream} throws an
     * exception on {@link TeeOutputStream#close()}.
     */
    @Test
    public void testIOExceptionOnCloseBranch() throws IOException {
        final OutputStream badOs = new YellOnCloseOutputStream();
        final ByteArrayOutputStream goodOs = mock(ByteArrayOutputStream.class);
        final TeeOutputStream tos = new TeeOutputStream(goodOs, badOs);
        try {
            tos.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodOs).close();
        }
    }

    /**
     * Tests that the main {@code OutputStream} is closed when closing the branch {@code OutputStream} throws an
     * exception on {@link TeeOutputStream#close()}.
     */
    @Test
    public void testIOExceptionOnClose() throws IOException {
        final OutputStream badOs = new YellOnCloseOutputStream();
        final ByteArrayOutputStream goodOs = mock(ByteArrayOutputStream.class);
        final TeeOutputStream tos = new TeeOutputStream(badOs, goodOs);
        try {
            tos.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodOs).close();
        }
    }

    @Test
    public void testTee() throws IOException {
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        final ByteArrayOutputStream expected = new ByteArrayOutputStream();

        try (final TeeOutputStream tos = new TeeOutputStream(baos1, baos2)) {
            for (int i = 0; i < 20; i++) {
                tos.write(i);
                expected.write(i);
            }
            assertByteArrayEquals("TeeOutputStream.write(int)", expected.toByteArray(), baos1.toByteArray());
            assertByteArrayEquals("TeeOutputStream.write(int)", expected.toByteArray(), baos2.toByteArray());

            final byte[] array = new byte[10];
            for (int i = 20; i < 30; i++) {
                array[i - 20] = (byte) i;
            }
            tos.write(array);
            expected.write(array);
            assertByteArrayEquals("TeeOutputStream.write(byte[])", expected.toByteArray(), baos1.toByteArray());
            assertByteArrayEquals("TeeOutputStream.write(byte[])", expected.toByteArray(), baos2.toByteArray());

            for (int i = 25; i < 35; i++) {
                array[i - 25] = (byte) i;
            }
            tos.write(array, 5, 5);
            expected.write(array, 5, 5);
            assertByteArrayEquals("TeeOutputStream.write(byte[], int, int)", expected.toByteArray(),
                    baos1.toByteArray());
            assertByteArrayEquals("TeeOutputStream.write(byte[], int, int)", expected.toByteArray(),
                    baos2.toByteArray());

            expected.flush();
            expected.close();

            tos.flush();
        }
    }

    private void assertByteArrayEquals(final String msg, final byte[] array1, final byte[] array2) {
        assertEquals(array1.length, array2.length, msg + ": array size mismatch");
        for (int i = 0; i < array1.length; i++) {
            assertEquals(array1[i], array2[i], msg + ": array[ " + i + "] mismatch");
        }
    }

}
