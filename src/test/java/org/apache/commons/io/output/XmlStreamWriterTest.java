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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Herve Boutemy
 * @version $Id$
 */
public class XmlStreamWriterTest extends TestCase {
    /** french */
    private static final String TEXT_LATIN1 = "eacute: \u00E9";
    /** greek */
    private static final String TEXT_LATIN7 = "alpha: \u03B1";
    /** euro support */
    private static final String TEXT_LATIN15 = "euro: \u20AC";
    /** japanese */
    private static final String TEXT_EUC_JP = "hiragana A: \u3042";
    /** Unicode: support everything */
    private static final String TEXT_UNICODE = TEXT_LATIN1 + ", " + TEXT_LATIN7
            + ", " + TEXT_LATIN15 + ", " + TEXT_EUC_JP;

    private static String createXmlContent(String text, String encoding) {
        String xmlDecl = "<?xml version=\"1.0\"?>";
        if (encoding != null) {
            xmlDecl = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
        }
        String xml = xmlDecl + "\n<text>" + text + "</text>";
        return xml;
    }

    private static void checkXmlContent(String xml, String encoding)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlStreamWriter writer = new XmlStreamWriter(out);
        writer.write(xml);
        writer.close();
        byte[] xmlContent = out.toByteArray();
        String result = new String(xmlContent, encoding);
        assertEquals(xml, result);
    }

    private static void checkXmlWriter(String text, String encoding)
            throws IOException {
        String xml = createXmlContent(text, encoding);
        String effectiveEncoding = (encoding == null) ? "UTF-8" : encoding;
        checkXmlContent(xml, effectiveEncoding);
    }

    public void testNoXmlHeader() throws IOException {
        String xml = "<text>text with no XML header</text>";
        checkXmlContent(xml, "UTF-8");
    }

    public void testEmpty() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlStreamWriter writer = new XmlStreamWriter(out);
        writer.flush();
        writer.write("");
        writer.flush();
        writer.write(".");
        writer.flush();
        writer.close();
    }

    public void testDefaultEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, null);
    }

    public void testUTF8Encoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, "UTF-8");
    }

    public void testUTF16Encoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, "UTF-16");
    }

    public void testUTF16BEEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, "UTF-16BE");
    }

    public void testUTF16LEEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, "UTF-16LE");
    }

    public void testLatin1Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN1, "ISO-8859-1");
    }

    public void testLatin7Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN7, "ISO-8859-7");
    }

    public void testLatin15Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN15, "ISO-8859-15");
    }

    public void testEUC_JPEncoding() throws IOException {
        checkXmlWriter(TEXT_EUC_JP, "EUC-JP");
    }

    public void testEBCDICEncoding() throws IOException {
        checkXmlWriter("simple text in EBCDIC", "CP1047");
    }
}
