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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.ByteOrderMark;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Test case for {@link BOMInputStream}.
 *
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class BOMInputStreamTest {
    //----------------------------------------------------------------------------
    //  Support code
    //----------------------------------------------------------------------------

    /**
     *  A mock InputStream that expects <code>close()</code> to be called.
     */
    private static class ExpectCloseInputStream extends InputStream {
        private boolean _closeCalled;

        public void assertCloseCalled() {
            assertTrue(_closeCalled);
        }

        @Override
        public void close() throws IOException {
            _closeCalled = true;
        }

        @Override
        public int read() throws IOException {
            return -1;
        }
    }

    private void assertData(final byte[] expected, final byte[] actual, final int len)
        throws Exception {
        assertEquals(expected.length, len, "length");
        for (int ii = 0; ii < expected.length; ii++) {
            assertEquals(expected[ii], actual[ii], "byte " + ii);
        }
    }

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    private InputStream createUtf16BeDataStream(final byte[] baseData, final boolean addBOM) {
        byte[] data = baseData;
        if (addBOM) {
            data = new byte[baseData.length + 2];
            data[0] = (byte) 0xFE;
            data[1] = (byte) 0xFF;
            System.arraycopy(baseData, 0, data, 2, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    private InputStream createUtf16LeDataStream(final byte[] baseData, final boolean addBOM) {
        byte[] data = baseData;
        if (addBOM) {
            data = new byte[baseData.length + 2];
            data[0] = (byte) 0xFF;
            data[1] = (byte) 0xFE;
            System.arraycopy(baseData, 0, data, 2, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    private InputStream createUtf32BeDataStream(final byte[] baseData, final boolean addBOM) {
        byte[] data = baseData;
        if (addBOM) {
            data = new byte[baseData.length + 4];
            data[0] = 0;
            data[1] = 0;
            data[2] = (byte) 0xFE;
            data[3] = (byte) 0xFF;
            System.arraycopy(baseData, 0, data, 4, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    private InputStream createUtf32LeDataStream(final byte[] baseData, final boolean addBOM) {
        byte[] data = baseData;
        if (addBOM) {
            data = new byte[baseData.length + 4];
            data[0] = (byte) 0xFF;
            data[1] = (byte) 0xFE;
            data[2] = 0;
            data[3] = 0;
            System.arraycopy(baseData, 0, data, 4, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    /**
     *  Creates the underlying data stream, with or without BOM.
     */
    private InputStream createUtf8DataStream(final byte[] baseData, final boolean addBOM) {
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

    //----------------------------------------------------------------------------
    //  Test cases
    //----------------------------------------------------------------------------

    private void parseXml(final InputStream in) throws SAXException, IOException, ParserConfigurationException {
        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(in));
        assertNotNull(doc);
        assertEquals("X", doc.getFirstChild().getNodeName());
    }

    private void parseXml(final Reader in) throws SAXException, IOException, ParserConfigurationException {
        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(in));
        assertNotNull(doc);
        assertEquals("X", doc.getFirstChild().getNodeName());
    }

    private void readBOMInputStreamTwice(final String resource) throws Exception {
        final InputStream inputStream = this.getClass().getResourceAsStream(resource);
        assertNotNull(inputStream);
        final BOMInputStream bomInputStream = new BOMInputStream(inputStream);
        bomInputStream.mark(1000000);

        this.readFile(bomInputStream);
        bomInputStream.reset();
        this.readFile(bomInputStream);
        inputStream.close();
        bomInputStream.close();
    }

    private void readFile(final BOMInputStream bomInputStream) throws Exception {
        int bytes;
        final byte[] bytesFromStream = new byte[100];
        do {
            bytes = bomInputStream.read(bytesFromStream);
        } while (bytes > 0);
    }

    @Test
    public void testAvailableWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            assertEquals(7, in.available());
        }
    }

    @Test
    public void testAvailableWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertEquals(4, in.available());
        }
    }

    @Test
    // this is here for coverage
    public void testClose() throws Exception {
        try (final ExpectCloseInputStream del = new ExpectCloseInputStream()) {
            try (final InputStream in = new BOMInputStream(del)) {
                // nothing
            }
            del.assertCloseCalled();
        }
    }

    @Test
    public void testEmptyBufferWithBOM() throws Exception {
        final byte[] data = new byte[] {};
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            final byte[] buf = new byte[1024];
            assertEquals(-1, in.read(buf));
        }
    }

    @Test
    public void testEmptyBufferWithoutBOM() throws Exception {
        final byte[] data = new byte[] {};
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            final byte[] buf = new byte[1024];
            assertEquals(-1, in.read(buf));
        }
    }

    @Test
    public void testGetBOMFirstThenRead() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            assertEquals(ByteOrderMark.UTF_8, in.getBOM(), "getBOM");
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
        }
    }

    @Test
    public void testGetBOMFirstThenReadInclude() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true), true)) {
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertEquals(ByteOrderMark.UTF_8, in.getBOM(), "getBOM");
            assertEquals(0xEF, in.read());
            assertEquals(0xBB, in.read());
            assertEquals(0xBF, in.read());
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
        }
    }

    @Test
    public void testLargeBufferWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            final byte[] buf = new byte[1024];
            assertData(data, buf, in.read(buf));
        }
    }

    @Test
    public void testLargeBufferWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            final byte[] buf = new byte[1024];
            assertData(data, buf, in.read(buf));
        }
    }

    @Test
    public void testLeadingNonBOMBufferedRead() throws Exception {
        final byte[] data = new byte[] { (byte) 0xEF, (byte) 0xAB, (byte) 0xCD };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            final byte[] buf = new byte[1024];
            assertData(data, buf, in.read(buf));
        }
    }

    @Test
    public void testLeadingNonBOMSingleRead() throws Exception {
        final byte[] data = new byte[] { (byte) 0xEF, (byte) 0xAB, (byte) 0xCD };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertEquals(0xEF, in.read());
            assertEquals(0xAB, in.read());
            assertEquals(0xCD, in.read());
            assertEquals(-1, in.read());
        }
    }

    @Test
    public void testMarkResetAfterReadWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            assertTrue(in.markSupported());

            in.read();
            in.mark(10);

            in.read();
            in.read();
            in.reset();
            assertEquals('B', in.read());
        }
    }

    @Test
    public void testMarkResetAfterReadWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertTrue(in.markSupported());

            in.read();
            in.mark(10);

            in.read();
            in.read();
            in.reset();
            assertEquals('B', in.read());
        }
    }

    @Test
    public void testMarkResetBeforeReadWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            assertTrue(in.markSupported());

            in.mark(10);

            in.read();
            in.read();
            in.reset();
            assertEquals('A', in.read());
        }
    }

    @Test
    public void testMarkResetBeforeReadWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertTrue(in.markSupported());

            in.mark(10);

            in.read();
            in.read();
            in.reset();
            assertEquals('A', in.read());
        }
    }

    @Test
    public void testNoBoms() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try {
            (new BOMInputStream(createUtf8DataStream(data, true), false, (ByteOrderMark[])null)).close();
            fail("Null BOMs, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            (new BOMInputStream(createUtf8DataStream(data, true), false, new ByteOrderMark[0])).close();
            fail("Null BOMs, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }





    @Test
    public void testReadEmpty() throws Exception {
        final byte[] data = new byte[] {};
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertEquals(-1, in.read());
            assertFalse(in.hasBOM(), "hasBOM()");
            assertFalse(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertNull(in.getBOM(), "getBOM");
        }
    }

    @Test
    public void testReadSmall() throws Exception {
        final byte[] data = new byte[] { 'A', 'B' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals(-1, in.read());
            assertFalse(in.hasBOM(), "hasBOM()");
            assertFalse(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertNull(in.getBOM(), "getBOM");
        }
    }

    @Test
    public void testReadTwiceWithBOM() throws Exception {
        this.readBOMInputStreamTwice("/org/apache/commons/io/testfileBOM.xml");
    }

    @Test
    public void testReadTwiceWithoutBOM() throws Exception {
        this.readBOMInputStreamTwice("/org/apache/commons/io/testfileNoBOM.xml");
    }

    @Test
    public void testReadWithBOMInclude() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true), true)) {
            assertEquals(0xEF, in.read());
            assertEquals(0xBB, in.read());
            assertEquals(0xBF, in.read());
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertEquals(ByteOrderMark.UTF_8, in.getBOM(), "getBOM");
        }
    }

    @Test
    public void testReadWithBOMUtf16Be() throws Exception {
        final byte[] data = "ABC".getBytes(StandardCharsets.UTF_16BE);
        try (final BOMInputStream in = new BOMInputStream(createUtf16BeDataStream(data, true),
                ByteOrderMark.UTF_16BE)) {
            assertEquals(0, in.read());
            assertEquals('A', in.read());
            assertEquals(0, in.read());
            assertEquals('B', in.read());
            assertEquals(0, in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_16BE), "hasBOM(UTF-16BE)");
            assertEquals(ByteOrderMark.UTF_16BE, in.getBOM(), "getBOM");
            try {
                in.hasBOM(ByteOrderMark.UTF_16LE);
                fail("Expected IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                // expected - not configured for UTF-16LE
            }
        }
    }

    @Test
    public void testReadWithBOMUtf16Le() throws Exception {
        final byte[] data = "ABC".getBytes(StandardCharsets.UTF_16LE);
        try (final BOMInputStream in = new BOMInputStream(createUtf16LeDataStream(data, true),
                ByteOrderMark.UTF_16LE)) {
            assertEquals('A', in.read());
            assertEquals(0, in.read());
            assertEquals('B', in.read());
            assertEquals(0, in.read());
            assertEquals('C', in.read());
            assertEquals(0, in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_16LE), "hasBOM(UTF-16LE)");
            assertEquals(ByteOrderMark.UTF_16LE, in.getBOM(), "getBOM");
            try {
                in.hasBOM(ByteOrderMark.UTF_16BE);
                fail("Expected IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                // expected - not configured for UTF-16BE
            }
        }
    }

    @Test
    public void testReadWithBOMUtf32Be() throws Exception {
        assumeTrue(Charset.isSupported("UTF_32BE"));
        final byte[] data = "ABC".getBytes("UTF_32BE");
        try (final BOMInputStream in = new BOMInputStream(createUtf32BeDataStream(data, true),
                ByteOrderMark.UTF_32BE)) {
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals('A', in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals('B', in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_32BE), "hasBOM(UTF-32BE)");
            assertEquals(ByteOrderMark.UTF_32BE, in.getBOM(), "getBOM");
            try {
                in.hasBOM(ByteOrderMark.UTF_32LE);
                fail("Expected IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                // expected - not configured for UTF-32LE
            }
        }
    }

    @Test
    public void testReadWithBOMUtf32Le() throws Exception {
        assumeTrue(Charset.isSupported("UTF_32LE"));
        final byte[] data = "ABC".getBytes("UTF_32LE");
        try (final BOMInputStream in = new BOMInputStream(createUtf32LeDataStream(data, true),
                ByteOrderMark.UTF_32LE)) {
            assertEquals('A', in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals('B', in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals('C', in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(0, in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_32LE), "hasBOM(UTF-32LE)");
            assertEquals(ByteOrderMark.UTF_32LE, in.getBOM(), "getBOM");
            try {
                in.hasBOM(ByteOrderMark.UTF_32BE);
                fail("Expected IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                // expected - not configured for UTF-32BE
            }
        }
    }

    @Test
    public void testReadWithBOMUtf8() throws Exception {
        final byte[] data = "ABC".getBytes(StandardCharsets.UTF_8);
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true), ByteOrderMark.UTF_8)) {
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertEquals(ByteOrderMark.UTF_8, in.getBOM(), "getBOM");
            try {
                in.hasBOM(ByteOrderMark.UTF_16BE);
                fail("Expected IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                // expected - not configured for UTF-16BE
            }
        }
    }

    @Test
    public void testReadWithMultipleBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true), ByteOrderMark.UTF_16BE,
                ByteOrderMark.UTF_8)) {
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertTrue(in.hasBOM(), "hasBOM()");
            assertTrue(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertFalse(in.hasBOM(ByteOrderMark.UTF_16BE), "hasBOM(UTF-16BE)");
            assertEquals(ByteOrderMark.UTF_8, in.getBOM(), "getBOM");
        }
    }

    @Test
    public void testReadWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            assertEquals('A', in.read());
            assertEquals('B', in.read());
            assertEquals('C', in.read());
            assertEquals(-1, in.read());
            assertFalse(in.hasBOM(), "hasBOM()");
            assertFalse(in.hasBOM(ByteOrderMark.UTF_8), "hasBOM(UTF-8)");
            assertNull(in.getBOM(), "getBOM");
        }
    }

    @Test
    public void testReadXmlWithBOMUcs2() throws Exception {
        assumeFalse(System.getProperty("java.vendor").contains("IBM"), "This test does not pass on some IBM VMs xml parsers");

        // UCS-2 is BE.
        assumeTrue(Charset.isSupported("ISO-10646-UCS-2"));
        final byte[] data = "<?xml version=\"1.0\" encoding=\"ISO-10646-UCS-2\"?><X/>".getBytes("ISO-10646-UCS-2");
        try (BOMInputStream in = new BOMInputStream(createUtf16BeDataStream(data, true), ByteOrderMark.UTF_16BE)) {
            parseXml(in);
        }
        parseXml(createUtf16BeDataStream(data, true));
    }

    @Test
    public void testReadXmlWithBOMUcs4() throws Exception {
        // UCS-4 is BE or LE?
        // Hm: ISO-10646-UCS-4 is not supported on Oracle 1.6.0_31
        assumeTrue(Charset.isSupported("ISO-10646-UCS-4"));
        final byte[] data = "<?xml version=\"1.0\" encoding=\"ISO-10646-UCS-4\"?><X/>".getBytes("ISO-10646-UCS-4");
        // XML parser does not know what to do with UTF-32
        try (BOMInputStream in = new BOMInputStream(createUtf32BeDataStream(data, true), ByteOrderMark.UTF_32BE)) {
            parseXml(in);
            // XML parser does not know what to do with UTF-32
            assumeTrue(jvmAndSaxBothSupportCharset("UTF_32LE"), "JVM and SAX need to support UTF_32LE for this");
        }
        parseXml(createUtf32BeDataStream(data, true));
    }

    @Test
    public void testReadXmlWithBOMUtf16Be() throws Exception {
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-16BE\"?><X/>".getBytes(StandardCharsets.UTF_16BE);
        try (BOMInputStream in = new BOMInputStream(createUtf16BeDataStream(data, true), ByteOrderMark.UTF_16BE)) {
            parseXml(in);
        }
        parseXml(createUtf16BeDataStream(data, true));
    }

    @Test
    public void testReadXmlWithBOMUtf16Le() throws Exception {
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-16LE\"?><X/>".getBytes(StandardCharsets.UTF_16LE);
        try (BOMInputStream in = new BOMInputStream(createUtf16LeDataStream(data, true), ByteOrderMark.UTF_16LE)) {
            parseXml(in);
        }
        parseXml(createUtf16LeDataStream(data, true));
    }

    @Test
    public void testReadXmlWithBOMUtf32Be() throws Exception {
        assumeTrue(jvmAndSaxBothSupportCharset("UTF_32BE"), "JVM and SAX need to support UTF_32BE for this");
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-32BE\"?><X/>".getBytes("UTF_32BE");
        try (BOMInputStream in = new BOMInputStream(createUtf32BeDataStream(data, true), ByteOrderMark.UTF_32BE)) {
            parseXml(in);
        }
        // XML parser does not know what to do with UTF-32, so we warp the input stream with a XmlStreamReader
        try (XmlStreamReader in = new XmlStreamReader(createUtf32BeDataStream(data, true))) {
            parseXml(in);
        }
    }

    @Test
    public void testReadXmlWithBOMUtf32Le() throws Exception {
        assumeTrue(jvmAndSaxBothSupportCharset("UTF_32LE"), "JVM and SAX need to support UTF_32LE for this");
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-32LE\"?><X/>".getBytes("UTF_32LE");
        try (BOMInputStream in = new BOMInputStream(createUtf32LeDataStream(data, true), ByteOrderMark.UTF_32LE)) {
            parseXml(in);
        }
        // XML parser does not know what to do with UTF-32, so we warp the input stream with a XmlStreamReader
        try (XmlStreamReader in = new XmlStreamReader(createUtf32LeDataStream(data, true))) {
            parseXml(in);
        }
    }

    @Test
    public void testReadXmlWithBOMUtf8() throws Exception {
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><X/>".getBytes(StandardCharsets.UTF_8);
        try (BOMInputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            parseXml(in);
        }
        parseXml(createUtf8DataStream(data, true));
    }

    @Test
    public void testReadXmlWithoutBOMUtf32Be() throws Exception {
        assumeTrue(jvmAndSaxBothSupportCharset("UTF_32BE"), "JVM and SAX need to support UTF_32BE for this");
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF_32BE\"?><X/>".getBytes("UTF_32BE");
        try (BOMInputStream in = new BOMInputStream(createUtf32BeDataStream(data, false))) {
            parseXml(in);
        }
        parseXml(createUtf32BeDataStream(data, false));
    }

    @Test
    public void testReadXmlWithoutBOMUtf32Le() throws Exception {
        assumeTrue(jvmAndSaxBothSupportCharset("UTF_32LE"), "JVM and SAX need to support UTF_32LE for this");
        final byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-32LE\"?><X/>".getBytes("UTF_32LE");
        try (BOMInputStream in = new BOMInputStream(createUtf32LeDataStream(data, false))) {
            parseXml(in);
        }
        parseXml(createUtf32BeDataStream(data, false));
    }

    @Test
    public void testSkipWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            in.skip(2L);
            assertEquals('C', in.read());
        }
    }

    @Test
    public void testSkipWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C', 'D' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            in.skip(2L);
            assertEquals('C', in.read());
        }
    }


    @Test
    public void skipReturnValueWithBom() throws IOException {
        final byte[] baseData = new byte[] { (byte) 0x31, (byte) 0x32, (byte) 0x33 };
        try (final BOMInputStream is1 = new BOMInputStream(createUtf8DataStream(baseData, true))) {
            assertEquals(2, is1.skip(2));
            assertEquals((byte) 0x33, is1.read());
        }
    }

    @Test
    public void skipReturnValueWithoutBom() throws IOException {
        final byte[] baseData = new byte[] { (byte) 0x31, (byte) 0x32, (byte) 0x33 };
        try (final BOMInputStream is2 = new BOMInputStream(createUtf8DataStream(baseData, false))) {
            assertEquals(2, is2.skip(2)); // IO-428
            assertEquals((byte) 0x33, is2.read());
        }
    }

    @Test
    public void testSmallBufferWithBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, true))) {
            final byte[] buf = new byte[1024];
            assertData(new byte[] { 'A', 'B' }, buf, in.read(buf, 0, 2));
            assertData(new byte[] { 'C' }, buf, in.read(buf, 0, 2));
        }
    }

    @Test
    public void testSmallBufferWithoutBOM() throws Exception {
        final byte[] data = new byte[] { 'A', 'B', 'C' };
        try (final InputStream in = new BOMInputStream(createUtf8DataStream(data, false))) {
            final byte[] buf = new byte[1024];
            assertData(new byte[] { 'A', 'B' }, buf, in.read(buf, 0, 2));
            assertData(new byte[] { 'C' }, buf, in.read(buf, 0, 2));
        }
    }

    @Test
    // make sure that our support code works as expected
    public void testSupportCode() throws Exception {
        try (final InputStream in = createUtf8DataStream(new byte[] { 'A', 'B' }, true)) {
            final byte[] buf = new byte[1024];
            final int len = in.read(buf);
            assertEquals(5, len);
            assertEquals(0xEF, buf[0] & 0xFF);
            assertEquals(0xBB, buf[1] & 0xFF);
            assertEquals(0xBF, buf[2] & 0xFF);
            assertEquals('A', buf[3] & 0xFF);
            assertEquals('B', buf[4] & 0xFF);

            assertData(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'A', 'B' }, buf, len);
        }
    }

    private boolean jvmAndSaxBothSupportCharset(final String charSetName) throws ParserConfigurationException, SAXException, IOException {
        return Charset.isSupported(charSetName) &&  doesSaxSupportCharacterSet(charSetName);
    }

    private boolean doesSaxSupportCharacterSet(final String charSetName) throws ParserConfigurationException, SAXException, IOException {
        final byte[] data = ("<?xml version=\"1.0\" encoding=\"" + charSetName + "\"?><Z/>").getBytes(charSetName);
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try {
            final InputSource is = new InputSource(new ByteArrayInputStream(data));
            is.setEncoding(charSetName);
            documentBuilder.parse(is);
        } catch (final SAXParseException e) {
            if (e.getMessage().contains(charSetName)) {
                return false;
            }
        }
        return true;
    }
}
