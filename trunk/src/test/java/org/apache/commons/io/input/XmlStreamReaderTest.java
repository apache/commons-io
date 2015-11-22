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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class XmlStreamReaderTest {
    private static final String XML5 = "xml-prolog-encoding-spaced-single-quotes";
    private static final String XML4 = "xml-prolog-encoding-single-quotes";
    private static final String XML3 = "xml-prolog-encoding-double-quotes";
    private static final String XML2 = "xml-prolog";
    private static final String XML1 = "xml";

    protected void _testRawNoBomValid(final String encoding) throws Exception {
        InputStream is = getXmlStream("no-bom", XML1, encoding, encoding);
        XmlStreamReader xmlReader = new XmlStreamReader(is, false);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();

        is = getXmlStream("no-bom", XML2, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();

        is = getXmlStream("no-bom", XML3, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();

        is = getXmlStream("no-bom", XML4, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();

        is = getXmlStream("no-bom", XML5, encoding, encoding);
        xmlReader = new XmlStreamReader(is);
        assertEquals(xmlReader.getEncoding(), encoding);
        xmlReader.close();
    }

    protected void _testRawNoBomInvalid(final String encoding) throws Exception {
        final InputStream is = getXmlStream("no-bom", XML3, encoding, encoding);
        try {
            (new XmlStreamReader(is, false)).close();;
            fail("It should have failed");
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
    }

    @Test
    public void testRawNoBomUsAscii() throws Exception {
        _testRawNoBomValid("US-ASCII");
    }

    @Test
    public void testRawNoBomUtf8() throws Exception {
        _testRawNoBomValid("UTF-8");
    }

    @Test
    public void testRawNoBomUtf16BE() throws Exception {
        _testRawNoBomValid("UTF-16BE");
    }

    @Test
    public void testRawNoBomUtf16LE() throws Exception {
        _testRawNoBomValid("UTF-16LE");
    }

    @Test
    public void testRawNoBomUtf32BE() throws Exception {
        _testRawNoBomValid("UTF-32BE");
    }

    @Test
    public void testRawNoBomUtf32LE() throws Exception {
        _testRawNoBomValid("UTF-32LE");
    }

    @Test
    public void testRawNoBomIso8859_1() throws Exception {
        _testRawNoBomValid("ISO-8859-1");
    }

    @Test
    public void testRawNoBomCp1047() throws Exception {
        _testRawNoBomValid("CP1047");
    }

    protected void _testRawBomValid(final String encoding) throws Exception {
        final InputStream is = getXmlStream(encoding + "-bom", XML3, encoding,
                encoding);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, false);
        if (!encoding.equals("UTF-16") && !encoding.equals("UTF-32")) {
            assertEquals(xmlReader.getEncoding(), encoding);
        } else {
            assertEquals(xmlReader.getEncoding()
                    .substring(0, encoding.length()), encoding);
        }
        xmlReader.close();
    }

    protected void _testRawBomInvalid(final String bomEnc, final String streamEnc,
                                      final String prologEnc) throws Exception {
        final InputStream is = getXmlStream(bomEnc, XML3, streamEnc, prologEnc);
        XmlStreamReader xmlReader = null;
        try {
            xmlReader = new XmlStreamReader(is, false);
            final String foundEnc = xmlReader.getEncoding();
            fail("Expected IOException for BOM " + bomEnc + ", streamEnc "
                    + streamEnc + " and prologEnc " + prologEnc + ": found "
                    + foundEnc);
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
        if (xmlReader != null) {
            xmlReader.close();
        }
    }

    @Test
    public void testRawBomUtf8() throws Exception {
        _testRawBomValid("UTF-8");
        _testRawBomInvalid("UTF-8-bom", "US-ASCII", "US-ASCII");
        _testRawBomInvalid("UTF-8-bom", "ISO-8859-1", "ISO-8859-1");
        _testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16");
        _testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16BE");
        _testRawBomInvalid("UTF-8-bom", "UTF-8", "UTF-16LE");
        _testRawBomInvalid("UTF-16BE-bom", "UTF-16BE", "UTF-16LE");
        _testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-16BE");
        _testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-8");
        _testRawBomInvalid("UTF-32BE-bom", "UTF-32BE", "UTF-32LE");
        _testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-32BE");
        _testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-8");
    }

    @Test
    public void testRawBomUtf16() throws Exception {
        _testRawBomValid("UTF-16BE");
        _testRawBomValid("UTF-16LE");
        _testRawBomValid("UTF-16");

        _testRawBomInvalid("UTF-16BE-bom", "UTF-16BE", "UTF-16LE");
        _testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-16BE");
        _testRawBomInvalid("UTF-16LE-bom", "UTF-16LE", "UTF-8");
    }

    @Test
    public void testRawBomUtf32() throws Exception {
        _testRawBomValid("UTF-32BE");
        _testRawBomValid("UTF-32LE");
        _testRawBomValid("UTF-32");

        _testRawBomInvalid("UTF-32BE-bom", "UTF-32BE", "UTF-32LE");
        _testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-32BE");
        _testRawBomInvalid("UTF-32LE-bom", "UTF-32LE", "UTF-8");
    }


    @Test
    public void testHttp() throws Exception {
        // niallp 2010-10-06 - remove following 2 tests - I reinstated
        // checks for non-UTF-16 encodings (18 tests) and these failed
        // _testHttpValid("application/xml", "no-bom", "US-ASCII", null);
        // _testHttpValid("application/xml", "UTF-8-bom", "US-ASCII", null);
        _testHttpValid("application/xml", "UTF-8-bom", "UTF-8", null);
        _testHttpValid("application/xml", "UTF-8-bom", "UTF-8", "UTF-8");
        _testHttpValid("application/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null);
        _testHttpValid("application/xml;charset=\"UTF-8\"", "UTF-8-bom", "UTF-8", null);
        _testHttpValid("application/xml;charset='UTF-8'", "UTF-8-bom", "UTF-8", null);
        _testHttpValid("application/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8");
        _testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null);
        _testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        _testHttpValid("application/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");

        _testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null);
        _testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        _testHttpInvalid("application/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");

        _testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null);
        _testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        _testHttpInvalid("application/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");

        _testHttpInvalid("application/xml", "UTF-8-bom", "US-ASCII", "US-ASCII");
        _testHttpInvalid("application/xml;charset=UTF-16", "UTF-16LE", "UTF-8", "UTF-8");
        _testHttpInvalid("application/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE");
        _testHttpInvalid("application/xml;charset=UTF-32", "UTF-32LE", "UTF-8", "UTF-8");
        _testHttpInvalid("application/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE");

        _testHttpValid("text/xml", "no-bom", "US-ASCII", null);
        _testHttpValid("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8");
        _testHttpValid("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null);
        _testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null);
        _testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        _testHttpValid("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");
        _testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", null);
        _testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        _testHttpValid("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");
        _testHttpValid("text/xml", "UTF-8-bom", "US-ASCII", null);

        _testAlternateDefaultEncoding("application/xml", "UTF-8-bom", "UTF-8", null, null);
        _testAlternateDefaultEncoding("application/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        _testAlternateDefaultEncoding("application/xml", "UTF-8-bom", "UTF-8", null, "UTF-8");
        _testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, null);
        _testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        _testAlternateDefaultEncoding("text/xml", "no-bom", "US-ASCII", null, "UTF-8");

        _testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null);
        _testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16");
        _testHttpInvalid("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE");
        _testHttpInvalid("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE");
        _testHttpInvalid("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", null);

        _testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null);
        _testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32");
        _testHttpInvalid("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE");
        _testHttpInvalid("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE");
        _testHttpInvalid("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", null);

        _testHttpLenient("text/xml", "no-bom", "US-ASCII", null, "US-ASCII");
        _testHttpLenient("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", "UTF-8", "UTF-8");
        _testHttpLenient("text/xml;charset=UTF-8", "UTF-8-bom", "UTF-8", null, "UTF-8");
        _testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", null, "UTF-16BE");
        _testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16", "UTF-16");
        _testHttpLenient("text/xml;charset=UTF-16", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        _testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", null, "UTF-32BE");
        _testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32", "UTF-32");
        _testHttpLenient("text/xml;charset=UTF-32", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        _testHttpLenient("text/xml", "UTF-8-bom", "US-ASCII", null, "US-ASCII");

        _testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", null, "UTF-16BE");
        _testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16", "UTF-16");
        _testHttpLenient("text/xml;charset=UTF-16BE", "UTF-16BE-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        _testHttpLenient("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", "UTF-16BE", "UTF-16BE");
        _testHttpLenient("text/xml;charset=UTF-16", "no-bom", "UTF-16BE", null, "UTF-16");

        _testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", null, "UTF-32BE");
        _testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32", "UTF-32");
        _testHttpLenient("text/xml;charset=UTF-32BE", "UTF-32BE-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        _testHttpLenient("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", "UTF-32BE", "UTF-32BE");
        _testHttpLenient("text/xml;charset=UTF-32", "no-bom", "UTF-32BE", null, "UTF-32");

        _testHttpLenient("text/html", "no-bom", "US-ASCII", "US-ASCII", "US-ASCII");
        _testHttpLenient("text/html", "no-bom", "US-ASCII", null, "US-ASCII");
        _testHttpLenient("text/html;charset=UTF-8", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
        _testHttpLenient("text/html;charset=UTF-16BE", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
        _testHttpLenient("text/html;charset=UTF-32BE", "no-bom", "US-ASCII", "UTF-8", "UTF-8");
    }

    @Test
    public void testRawContent() throws Exception {
        final String encoding = "UTF-8";
        final String xml = getXML("no-bom", XML3, encoding, encoding);
        final ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding));
        final XmlStreamReader xmlReader = new XmlStreamReader(is);
        assertEquals("Check encoding", xmlReader.getEncoding(), encoding);
        assertEquals("Check content", xml, IOUtils.toString(xmlReader));
    }

    @Test
    public void testHttpContent() throws Exception {
        final String encoding = "UTF-8";
        final String xml = getXML("no-bom", XML3, encoding, encoding);
        final ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding));
        final XmlStreamReader xmlReader = new XmlStreamReader(is, encoding);
        assertEquals("Check encoding", xmlReader.getEncoding(), encoding);
        assertEquals("Check content", xml, IOUtils.toString(xmlReader));
    }

    public void _testAlternateDefaultEncoding(final String cT, final String bomEnc,
                                              final String streamEnc, final String prologEnc, final String alternateEnc)
            throws Exception {
        final InputStream is = getXmlStream(bomEnc, prologEnc == null ? XML1
                : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, false, alternateEnc);
        if (!streamEnc.equals("UTF-16")) {
            // we can not assert things here because UTF-8, US-ASCII and
            // ISO-8859-1 look alike for the chars used for detection
            // (niallp 2010-10-06 - I re-instated the check below - the tests(6) passed)
            final String enc = alternateEnc != null ? alternateEnc : streamEnc;
            assertEquals(xmlReader.getEncoding(), enc);
        } else {
            //String enc = (alternateEnc != null) ? alternateEnc : streamEnc;
            assertEquals(xmlReader.getEncoding().substring(0,
                    streamEnc.length()), streamEnc);
        }
        xmlReader.close();
    }

    public void _testHttpValid(final String cT, final String bomEnc, final String streamEnc,
                               final String prologEnc) throws Exception {
        final InputStream is = getXmlStream(bomEnc,
                prologEnc == null ? XML1 : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, false);
        if (!streamEnc.equals("UTF-16")) {
            // we can not assert things here because UTF-8, US-ASCII and
            // ISO-8859-1 look alike for the chars used for detection
            // (niallp 2010-10-06 - I re-instated the check below and removed the 2 tests that failed)
            assertEquals(xmlReader.getEncoding(), streamEnc);
        } else {
            assertEquals(xmlReader.getEncoding().substring(0,
                    streamEnc.length()), streamEnc);
        }
        xmlReader.close();
    }

    protected void _testHttpInvalid(final String cT, final String bomEnc, final String streamEnc,
                                    final String prologEnc) throws Exception {
        final InputStream is = getXmlStream(bomEnc,
                prologEnc == null ? XML2 : XML3, streamEnc, prologEnc);
        try {
            (new XmlStreamReader(is, cT, false)).close();;
            fail("It should have failed for HTTP Content-type " + cT + ", BOM "
                    + bomEnc + ", streamEnc " + streamEnc + " and prologEnc "
                    + prologEnc);
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("Invalid encoding,"));
        }
    }

    protected void _testHttpLenient(final String cT, final String bomEnc, final String streamEnc,
                                    final String prologEnc, final String shouldbe) throws Exception {
        final InputStream is = getXmlStream(bomEnc,
                prologEnc == null ? XML2 : XML3, streamEnc, prologEnc);
        final XmlStreamReader xmlReader = new XmlStreamReader(is, cT, true);
        assertEquals(xmlReader.getEncoding(), shouldbe);
        xmlReader.close();
    }

    private static final String ENCODING_ATTRIBUTE_XML = "<?xml version=\"1.0\" ?> \n"
            + "<atom:feed xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
            + "\n"
            + "  <atom:entry>\n"
            + "    <atom:title encoding='base64'><![CDATA\n"
            + "aW5nTGluZSIgLz4";

    @Test
    public void testEncodingAttributeXML() throws Exception {
        final InputStream is = new ByteArrayInputStream(ENCODING_ATTRIBUTE_XML
                .getBytes("UTF-8"));
        final XmlStreamReader xmlReader = new XmlStreamReader(is, "", true);
        assertEquals(xmlReader.getEncoding(), "UTF-8");
        xmlReader.close();
    }

    // XML Stream generator

    private static final int[] NO_BOM_BYTES = {};
    private static final int[] UTF_16BE_BOM_BYTES = {0xFE, 0xFF};
    private static final int[] UTF_16LE_BOM_BYTES = {0xFF, 0XFE};
    private static final int[] UTF_32BE_BOM_BYTES = {0x00, 0x00, 0xFE, 0xFF};
    private static final int[] UTF_32LE_BOM_BYTES = {0xFF, 0XFE, 0x00, 0x00};
    private static final int[] UTF_8_BOM_BYTES = {0xEF, 0xBB, 0xBF};

    private static final Map<String, int[]> BOMs = new HashMap<String, int[]>();

    static {
        BOMs.put("no-bom", NO_BOM_BYTES);
        BOMs.put("UTF-16BE-bom", UTF_16BE_BOM_BYTES);
        BOMs.put("UTF-16LE-bom", UTF_16LE_BOM_BYTES);
        BOMs.put("UTF-32BE-bom", UTF_32BE_BOM_BYTES);
        BOMs.put("UTF-32LE-bom", UTF_32LE_BOM_BYTES);
        BOMs.put("UTF-16-bom", NO_BOM_BYTES); // it's added by the writer
        BOMs.put("UTF-8-bom", UTF_8_BOM_BYTES);
    }

    private static final MessageFormat XML = new MessageFormat(
            "<root>{2}</root>");
    private static final MessageFormat XML_WITH_PROLOG = new MessageFormat(
            "<?xml version=\"1.0\"?>\n<root>{2}</root>");
    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_DOUBLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding=\"{1}\"?>\n<root>{2}</root>");
    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_SINGLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding=''{1}''?>\n<root>{2}</root>");
    private static final MessageFormat XML_WITH_PROLOG_AND_ENCODING_SPACED_SINGLE_QUOTES = new MessageFormat(
            "<?xml version=\"1.0\" encoding =  \t \n \r''{1}''?>\n<root>{2}</root>");

    private static final MessageFormat INFO = new MessageFormat(
            "\nBOM : {0}\nDoc : {1}\nStream Enc : {2}\nProlog Enc : {3}\n");

    private static final Map<String, MessageFormat> XMLs = new HashMap<String, MessageFormat>();

    static {
        XMLs.put(XML1, XML);
        XMLs.put(XML2, XML_WITH_PROLOG);
        XMLs.put(XML3, XML_WITH_PROLOG_AND_ENCODING_DOUBLE_QUOTES);
        XMLs.put(XML4, XML_WITH_PROLOG_AND_ENCODING_SINGLE_QUOTES);
        XMLs.put(XML5, XML_WITH_PROLOG_AND_ENCODING_SPACED_SINGLE_QUOTES);
    }

    /**
     * @param bomType   no-bom, UTF-16BE-bom, UTF-16LE-bom, UTF-8-bom
     * @param xmlType   xml, xml-prolog, xml-prolog-charset
     * @param streamEnc encoding of the stream
     * @param prologEnc encoding of the prolog
     * @return XML stream
     * @throws IOException If an I/O error occurs
     */
    protected InputStream getXmlStream(final String bomType, final String xmlType,
                                       final String streamEnc, final String prologEnc) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        int[] bom = BOMs.get(bomType);
        if (bom == null) {
            bom = new int[0];
        }
        for (final int element : bom) {
            baos.write(element);
        }
        final Writer writer = new OutputStreamWriter(baos, streamEnc);
        final String xmlDoc = getXML(bomType, xmlType, streamEnc, prologEnc);
        writer.write(xmlDoc);

        // PADDDING TO TEST THINGS WORK BEYOND PUSHBACK_SIZE
        writer.write("<da>\n");
        for (int i = 0; i < 10000; i++) {
            writer.write("<do/>\n");
        }
        writer.write("</da>\n");

        writer.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Create the XML.
     */
    private String getXML(final String bomType, final String xmlType,
                          final String streamEnc, final String prologEnc) {
        final MessageFormat xml = XMLs.get(xmlType);
        final String info = INFO.format(new Object[]{bomType, xmlType, prologEnc});
        final String xmlDoc = xml.format(new Object[]{streamEnc, prologEnc, info});
        return xmlDoc;
    }
}
