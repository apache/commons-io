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
package org.apache.commons.io.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class BOMExclusionInputStreamTest extends TestCase {
    //----------------------------------------------------------------------------
    //  Support code
    //----------------------------------------------------------------------------

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    public InputStream createDataStream(byte[] baseData, boolean addBOM) {
        byte[] data = baseData;
        if (addBOM) {
            data = new byte[baseData.length + 3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) 0xBB;
            data[2] = (byte) 0xBF;
            System.arraycopy(baseData, 0, data, 3, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    private void assertData(byte[] expected, byte[] actual, int off, int len)
        throws Exception {
        assertEquals("length", expected.length, len);
        for (int ii = 0; ii < expected.length; ii++) {
            assertEquals("byte " + ii, expected[ii], actual[ii]);
        }
    }

    /**
     *  A mock InputStream that expects <code>close()</code> to be called.
     */
    private static class ExpectCloseInputStream extends InputStream {
        private boolean _closeCalled;

        @Override
        public void close() throws IOException {
            _closeCalled = true;
        }

        @Override
        public int read() throws IOException {
            return -1;
        }

        public void assertCloseCalled() {
            assertTrue(_closeCalled);
        }
    }

    //----------------------------------------------------------------------------
    //  Test cases
    //----------------------------------------------------------------------------

    // make sure that our support code works as expected
    public void testSupportCode() throws Exception {
        InputStream in = createDataStream(new byte[] { 'A', 'B' }, true);
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        assertEquals(5, len);
        assertEquals(0xEF, buf[0] & 0xFF);
        assertEquals(0xBB, buf[1] & 0xFF);
        assertEquals(0xBF, buf[2] & 0xFF);
        assertEquals('A', buf[3] & 0xFF);
        assertEquals('B', buf[4] & 0xFF);

        assertData(
                new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'A', 'B' },
                buf, 0, len);
    }

    public void testReadWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals(-1, in.read());
    }

    public void testReadWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals(-1, in.read());
    }

    public void testLargeBufferWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        byte[] buf = new byte[1024];
        assertData(data, buf, 0, in.read(buf));
    }

    public void testLargeBufferWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        byte[] buf = new byte[1024];
        assertData(data, buf, 0, in.read(buf));
    }

    public void testSmallBufferWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        byte[] buf = new byte[1024];
        assertData(new byte[] { 'A', 'B' }, buf, 0, in.read(buf, 0, 2));
        assertData(new byte[] { 'C' }, buf, 0, in.read(buf, 0, 2));
    }

    public void testSmallBufferWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        byte[] buf = new byte[1024];
        assertData(new byte[] { 'A', 'B' }, buf, 0, in.read(buf, 0, 2));
        assertData(new byte[] { 'C' }, buf, 0, in.read(buf, 0, 2));
    }

    public void testLeadingNonBOMSingleRead() throws Exception {
        byte[] data = new byte[] { (byte) 0xEF, (byte) 0xAB, (byte) 0xCD };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        assertEquals(0xEF, in.read());
        assertEquals(0xAB, in.read());
        assertEquals(0xCD, in.read());
        assertEquals(-1, in.read());
    }

    public void testLeadingNonBOMBufferedRead() throws Exception {
        byte[] data = new byte[] { (byte) 0xEF, (byte) 0xAB, (byte) 0xCD };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        byte[] buf = new byte[1024];
        assertData(data, buf, 0, in.read(buf));
    }

    public void testSkipWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        in.skip(2L);
        assertEquals('C', in.read());
    }

    public void testSkipWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        in.skip(2L);
        assertEquals('C', in.read());
    }

    public void testMarkResetAfterReadWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        assertTrue(in.markSupported());

        in.read();
        in.mark(10);

        in.read();
        in.read();
        in.reset();
        assertEquals('B', in.read());
    }

    public void testMarkResetAfterReadWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        assertTrue(in.markSupported());

        in.read();
        in.mark(10);

        in.read();
        in.read();
        in.reset();
        assertEquals('B', in.read());
    }

    public void testMarkResetBeforeReadWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        assertTrue(in.markSupported());

        in.mark(10);

        in.read();
        in.read();
        in.reset();
        assertEquals('A', in.read());
    }

    public void testMarkResetBeforeReadWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        assertTrue(in.markSupported());

        in.mark(10);

        in.read();
        in.read();
        in.reset();
        assertEquals('A', in.read());
    }

    public void testAvailableWithoutBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, false));
        assertEquals(4, in.available());
    }

    public void testAvailableWithBOM() throws Exception {
        byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        InputStream in = new BOMExclusionInputStream(createDataStream(data, true));
        assertEquals(7, in.available());
    }

    // this is here for coverage
    public void testClose() throws Exception {
        ExpectCloseInputStream del = new ExpectCloseInputStream();
        InputStream in = new BOMExclusionInputStream(del);

        in.close();
        del.assertCloseCalled();
    }
}
