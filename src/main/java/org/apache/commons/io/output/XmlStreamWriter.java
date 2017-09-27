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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.XmlStreamReader;

/**
 * Character stream that handles all the necessary Voodoo to figure out the
 * charset encoding of the XML document written to the stream.
 *
 * @see XmlStreamReader
 * @since 2.0
 */
public class XmlStreamWriter extends Writer {
    private static final int BUFFER_SIZE = 4096;

    private final OutputStream out;

    private final String defaultEncoding;

    private StringWriter xmlPrologWriter = new StringWriter(BUFFER_SIZE);

    private Writer writer;

    private String encoding;

    /**
     * Constructs a new XML stream writer for the specified output stream
     * with a default encoding of UTF-8.
     *
     * @param out The output stream
     */
    public XmlStreamWriter(final OutputStream out) {
        this(out, null);
    }

    /**
     * Constructs a new XML stream writer for the specified output stream
     * with the specified default encoding.
     *
     * @param out The output stream
     * @param defaultEncoding The default encoding if not encoding could be detected
     */
    public XmlStreamWriter(final OutputStream out, final String defaultEncoding) {
        this.out = out;
        this.defaultEncoding = defaultEncoding != null ? defaultEncoding : "UTF-8";
    }

    /**
     * Constructs a new XML stream writer for the specified file
     * with a default encoding of UTF-8.
     *
     * @param file The file to write to
     * @throws FileNotFoundException if there is an error creating or
     * opening the file
     */
    public XmlStreamWriter(final File file) throws FileNotFoundException {
        this(file, null);
    }

    /**
     * Constructs a new XML stream writer for the specified file
     * with the specified default encoding.
     *
     * @param file The file to write to
     * @param defaultEncoding The default encoding if not encoding could be detected
     * @throws FileNotFoundException if there is an error creating or
     * opening the file
     */
    public XmlStreamWriter(final File file, final String defaultEncoding) throws FileNotFoundException {
        this(new FileOutputStream(file), defaultEncoding);
    }

    /**
     * Returns the detected encoding.
     *
     * @return the detected encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Closes the underlying writer.
     *
     * @throws IOException if an error occurs closing the underlying writer
     */
    @Override
    public void close() throws IOException {
        if (writer == null) {
            encoding = defaultEncoding;
            writer = new OutputStreamWriter(out, encoding);
            writer.write(xmlPrologWriter.toString());
        }
        writer.close();
    }

    /**
     * Flushes the underlying writer.
     *
     * @throws IOException if an error occurs flushing the underlying writer
     */
    @Override
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    /**
     * Detects the encoding.
     *
     * @param cbuf the buffer to write the characters from
     * @param off The start offset
     * @param len The number of characters to write
     * @throws IOException if an error occurs detecting the encoding
     */
    private void detectEncoding(final char[] cbuf, final int off, final int len)
            throws IOException {
        int size = len;
        final StringBuffer xmlProlog = xmlPrologWriter.getBuffer();
        if (xmlProlog.length() + len > BUFFER_SIZE) {
            size = BUFFER_SIZE - xmlProlog.length();
        }
        xmlPrologWriter.write(cbuf, off, size);

        // try to determine encoding
        if (xmlProlog.length() >= 5) {
            if (xmlProlog.substring(0, 5).equals("<?xml")) {
                // try to extract encoding from XML prolog
                final int xmlPrologEnd = xmlProlog.indexOf("?>");
                if (xmlPrologEnd > 0) {
                    // ok, full XML prolog written: let's extract encoding
                    final Matcher m = ENCODING_PATTERN.matcher(xmlProlog.substring(0,
                            xmlPrologEnd));
                    if (m.find()) {
                        encoding = m.group(1).toUpperCase();
                        encoding = encoding.substring(1, encoding.length() - 1);
                    } else {
                        // no encoding found in XML prolog: using default
                        // encoding
                        encoding = defaultEncoding;
                    }
                } else {
                    if (xmlProlog.length() >= BUFFER_SIZE) {
                        // no encoding found in first characters: using default
                        // encoding
                        encoding = defaultEncoding;
                    }
                }
            } else {
                // no XML prolog: using default encoding
                encoding = defaultEncoding;
            }
            if (encoding != null) {
                // encoding has been chosen: let's do it
                xmlPrologWriter = null;
                writer = new OutputStreamWriter(out, encoding);
                writer.write(xmlProlog.toString());
                if (len > size) {
                    writer.write(cbuf, off + size, len - size);
                }
            }
        }
    }

    /**
     * Writes the characters to the underlying writer, detecting encoding.
     *
     * @param cbuf the buffer to write the characters from
     * @param off The start offset
     * @param len The number of characters to write
     * @throws IOException if an error occurs detecting the encoding
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        if (xmlPrologWriter != null) {
            detectEncoding(cbuf, off, len);
        } else {
            writer.write(cbuf, off, len);
        }
    }

    static final Pattern ENCODING_PATTERN = XmlStreamReader.ENCODING_PATTERN;
}
