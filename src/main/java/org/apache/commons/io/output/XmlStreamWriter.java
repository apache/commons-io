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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.XmlStreamReader;

/**
 * Character stream that handles all the necessary work to figure out the charset encoding of the XML document written to the stream.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @see XmlStreamReader
 * @since 2.0
 */
public class XmlStreamWriter extends Writer {

    // @formatter:off
    /**
     * Builds a new {@link XmlStreamWriter}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * WriterOutputStream w = WriterOutputStream.builder()
     *   .setPath(path)
     *   .setCharset(StandardCharsets.UTF_8)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:off
    public static class Builder extends AbstractStreamBuilder<XmlStreamWriter, Builder> {

        /**
         * Constructs a new {@link Builder}.
         */
        public Builder() {
            setCharsetDefault(StandardCharsets.UTF_8);
            setCharset(StandardCharsets.UTF_8);
        }

        /**
         * Builds a new {@link XmlStreamWriter}.
         * <p>
         * You must set input that supports {@link #getOutputStream()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getOutputStream()}</li>
         * <li>{@link #getCharset()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link OutputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getOutputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public XmlStreamWriter get() throws IOException {
            return new XmlStreamWriter(getOutputStream(), getCharset());
        }

    }

    private static final int BUFFER_SIZE = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final OutputStream out;

    private final Charset defaultCharset;

    private StringWriter prologWriter = new StringWriter(BUFFER_SIZE);

    private Writer writer;

    private Charset charset;

    /**
     * Constructs a new XML stream writer for the specified file
     * with a default encoding of UTF-8.
     *
     * @param file The file to write to
     * @throws FileNotFoundException if there is an error creating or
     * opening the file
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
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
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    @SuppressWarnings("resource")
    public XmlStreamWriter(final File file, final String defaultEncoding) throws FileNotFoundException {
        this(new FileOutputStream(file), defaultEncoding);
    }

    /**
     * Constructs a new XML stream writer for the specified output stream
     * with a default encoding of UTF-8.
     *
     * @param out The output stream
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamWriter(final OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    /**
     * Constructs a new XML stream writer for the specified output stream
     * with the specified default encoding.
     *
     * @param out The output stream
     * @param defaultEncoding The default encoding if not encoding could be detected
     */
    private XmlStreamWriter(final OutputStream out, final Charset defaultEncoding) {
        this.out = out;
        this.defaultCharset = Objects.requireNonNull(defaultEncoding);
    }

    /**
     * Constructs a new XML stream writer for the specified output stream
     * with the specified default encoding.
     *
     * @param out The output stream
     * @param defaultEncoding The default encoding if not encoding could be detected
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamWriter(final OutputStream out, final String defaultEncoding) {
        this(out, Charsets.toCharset(defaultEncoding, StandardCharsets.UTF_8));
    }

    /**
     * Closes the underlying writer.
     *
     * @throws IOException if an error occurs closing the underlying writer
     */
    @Override
    public void close() throws IOException {
        if (writer == null) {
            charset = defaultCharset;
            writer = new OutputStreamWriter(out, charset);
            writer.write(prologWriter.toString());
        }
        writer.close();
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
        final StringBuffer xmlProlog = prologWriter.getBuffer();
        if (xmlProlog.length() + len > BUFFER_SIZE) {
            size = BUFFER_SIZE - xmlProlog.length();
        }
        prologWriter.write(cbuf, off, size);

        // try to determine encoding
        if (xmlProlog.length() >= 5) {
            if (xmlProlog.substring(0, 5).equals("<?xml")) {
                // try to extract encoding from XML prolog
                final int xmlPrologEnd = xmlProlog.indexOf("?>");
                if (xmlPrologEnd > 0) {
                    // ok, full XML prolog written: let's extract encoding
                    final Matcher m = XmlStreamReader.ENCODING_PATTERN.matcher(xmlProlog.substring(0,
                            xmlPrologEnd));
                    if (m.find()) {
                        final String encName = m.group(1).toUpperCase(Locale.ROOT);
                        charset = Charset.forName(encName.substring(1, encName.length() - 1));
                    } else {
                        // no encoding found in XML prolog: using default
                        // encoding
                        charset = defaultCharset;
                    }
                } else if (xmlProlog.length() >= BUFFER_SIZE) {
                    // no encoding found in first characters: using default
                    // encoding
                    charset = defaultCharset;
                }
            } else {
                // no XML prolog: using default encoding
                charset = defaultCharset;
            }
            if (charset != null) {
                // encoding has been chosen: let's do it
                prologWriter = null;
                writer = new OutputStreamWriter(out, charset);
                writer.write(xmlProlog.toString());
                if (len > size) {
                    writer.write(cbuf, off + size, len - size);
                }
            }
        }
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
     * Returns the default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultCharset.name();
    }

    /**
     * Returns the detected encoding.
     *
     * @return the detected encoding
     */
    public String getEncoding() {
        return charset.name();
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
        if (prologWriter != null) {
            detectEncoding(cbuf, off, len);
        } else {
            writer.write(cbuf, off, len);
        }
    }
}
