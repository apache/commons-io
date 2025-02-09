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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

/**
 * Tests {@link XmlStreamWriter}.
 */
public class XmlStreamWriterTest {

    /** French */
    private static final String TEXT_LATIN1 = "eacute: \u00E9";

    /** Greek */
    private static final String TEXT_LATIN7 = "alpha: \u03B1";

    /** Euro support */
    private static final String TEXT_LATIN15 = "euro: \u20AC";

    /** Japanese */
    private static final String TEXT_EUC_JP = "hiragana A: \u3042";

    /** Unicode: support everything */
    private static final String TEXT_UNICODE = TEXT_LATIN1 + ", " + TEXT_LATIN7
            + ", " + TEXT_LATIN15 + ", " + TEXT_EUC_JP;

    @SuppressWarnings("resource")
    private static void checkXmlContent(final String xml, final String encodingName, final String defaultEncodingName)
            throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XmlStreamWriter writerCheck;
        try (XmlStreamWriter writer = XmlStreamWriter.builder().setOutputStream(out).setCharset(defaultEncodingName).get()) {
            writerCheck = writer;
            writer.write(xml);
        }
        final byte[] xmlContent = out.toByteArray();
        final Charset charset = Charset.forName(encodingName);
        final Charset writerCharset = Charset.forName(writerCheck.getEncoding());
        assertEquals(charset, writerCharset);
        assertTrue(writerCharset.contains(charset), writerCharset.name());
        assertArrayEquals(xml.getBytes(encodingName), xmlContent);
    }

    private static void checkXmlWriter(final String text, final String encoding)
            throws IOException {
        checkXmlWriter(text, encoding, null);
    }

    private static void checkXmlWriter(final String text, final String encoding, final String defaultEncoding)
            throws IOException {
        final String xml = createXmlContent(text, encoding);
        String effectiveEncoding = encoding;
        if (effectiveEncoding == null) {
            effectiveEncoding = defaultEncoding == null ? StandardCharsets.UTF_8.name() : defaultEncoding;
        }
        checkXmlContent(xml, effectiveEncoding, defaultEncoding);
    }

    private static String createXmlContent(final String text, final String encoding) {
        String xmlDecl = "<?xml version=\"1.0\"?>";
        if (encoding != null) {
            xmlDecl = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
        }
        return xmlDecl + "\n<text>" + text + "</text>";
    }

    @Test
    public void testDefaultEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, null, null);
        checkXmlWriter(TEXT_UNICODE, null, StandardCharsets.UTF_8.name());
        checkXmlWriter(TEXT_UNICODE, null, StandardCharsets.UTF_16.name());
        checkXmlWriter(TEXT_UNICODE, null, StandardCharsets.UTF_16BE.name());
        checkXmlWriter(TEXT_UNICODE, null, StandardCharsets.ISO_8859_1.name());
    }

    @Test
    public void testEBCDICEncoding() throws IOException {
        checkXmlWriter("simple text in EBCDIC", "CP1047");
    }

    @Test
    public void testEmpty() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                XmlStreamWriter writer = new XmlStreamWriter(out)) {
            writer.flush();
            writer.write("");
            writer.flush();
            writer.write(".");
            writer.flush();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                XmlStreamWriter writer = XmlStreamWriter.builder().setOutputStream(out).get()) {
            writer.flush();
            writer.write("");
            writer.flush();
            writer.write(".");
            writer.flush();
        }
    }

    @Test
    public void testEUC_JPEncoding() throws IOException {
        checkXmlWriter(TEXT_EUC_JP, "EUC-JP");
    }

    @Test
    public void testLatin15Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN15, "ISO-8859-15");
    }

    @Test
    public void testLatin1Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN1, StandardCharsets.ISO_8859_1.name());
    }

    @Test
    public void testLatin7Encoding() throws IOException {
        checkXmlWriter(TEXT_LATIN7, "ISO-8859-7");
    }

    /** Turkish language has specific rules to convert dotted and dotless i character. */
    @Test
    @DefaultLocale(language = "tr")
    public void testLowerCaseEncodingWithTurkishLocale_IO_557() throws IOException {
        checkXmlWriter(TEXT_UNICODE, "utf-8");
        checkXmlWriter(TEXT_LATIN1, "iso-8859-1");
        checkXmlWriter(TEXT_LATIN7, "iso-8859-7");
    }

    @Test
    public void testNoXmlHeader() throws IOException {
        checkXmlContent("<text>text with no XML header</text>", StandardCharsets.UTF_8.name(), null);
    }

    @Test
    public void testUTF16BEEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, StandardCharsets.UTF_16BE.name());
    }

    @Test
    public void testUTF16Encoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, StandardCharsets.UTF_16.name());
    }

    @Test
    public void testUTF16LEEncoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, StandardCharsets.UTF_16LE.name());
    }

    @Test
    public void testUTF8Encoding() throws IOException {
        checkXmlWriter(TEXT_UNICODE, StandardCharsets.UTF_8.name());
    }
}
