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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.output.XmlStreamWriter;

/**
 * Character stream that handles all the necessary Voodoo to figure out the charset encoding of the XML document within the stream.
 * <p>
 * IMPORTANT: This class is not related in any way to the org.xml.sax.XMLReader. This one IS a character stream.
 * </p>
 * <p>
 * All this has to be done without consuming characters from the stream, if not the XML parser will not recognized the document as a valid XML. This is not 100%
 * true, but it's close enough (UTF-8 BOM is not handled by all parsers right now, XmlStreamReader handles it and things work in all parsers).
 * </p>
 * <p>
 * The XmlStreamReader class handles the charset encoding of XML documents in Files, raw streams and HTTP streams by offering a wide set of constructors.
 * </p>
 * <p>
 * By default the charset encoding detection is lenient, the constructor with the lenient flag can be used for a script (following HTTP MIME and XML
 * specifications). All this is nicely explained by Mark Pilgrim in his blog, <a href="https://diveintomark.org/archives/2004/02/13/xml-media-types">
 * Determining the character encoding of a feed</a>.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * Originally developed for <a href="https://rome.dev.java.net">ROME</a> under Apache License 2.0.
 * </p>
 *
 * @see Builder
 * @see org.apache.commons.io.output.XmlStreamWriter
 * @since 2.0
 */
public class XmlStreamReader extends Reader {

    // @formatter:off
    /**
     * Builds a new {@link XmlStreamWriter}.
     *
     * Constructs a Reader using an InputStream and the associated content-type header. This constructor is lenient regarding the encoding detection.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the content-type encoding. If there is not content-type encoding checks the XML prolog
     * encoding. If there is not XML prolog encoding uses the default encoding mandated by the content-type MIME type.
     * </p>
     * <p>
     * If lenient detection is indicated and the detection above fails as per specifications it then attempts the following:
     * </p>
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and tries the detection again.
     * </p>
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else 'UTF-8' is used.
     * </p>
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never thrown.
     * </p>
     * <p>
     * For example:
     * </p>
     *
     * <pre>{@code
     * XmlStreamReader r = XmlStreamReader.builder()
     *   .setPath(path)
     *   .setCharset(StandardCharsets.UTF_8)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<XmlStreamReader, Builder> {

        private boolean nullCharset = true;
        private boolean lenient = true;
        private String httpContentType;

        /**
         * Builds a new {@link XmlStreamWriter}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link #getCharset()}</li>
         * <li>lenient</li>
         * <li>httpContentType</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @throws XmlStreamReaderException thrown if the Charset encoding could not be determined according to the specification.
         * @see #getInputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public XmlStreamReader get() throws IOException {
            final String defaultEncoding = nullCharset ? null : getCharset().name();
            // @formatter:off
            return httpContentType == null
                    ? new XmlStreamReader(getInputStream(), lenient, defaultEncoding)
                    : new XmlStreamReader(getInputStream(), httpContentType, lenient, defaultEncoding);
            // @formatter:on
        }

        @Override
        public Builder setCharset(final Charset charset) {
            nullCharset = charset == null;
            return super.setCharset(charset);
        }

        @Override
        public Builder setCharset(final String charset) {
            nullCharset = charset == null;
            return super.setCharset(Charsets.toCharset(charset, getCharsetDefault()));
        }

        /**
         * Sets the HTTP content type.
         *
         * @param httpContentType the HTTP content type.
         * @return {@code this} instance.
         */
        public Builder setHttpContentType(final String httpContentType) {
            this.httpContentType = httpContentType;
            return this;
        }

        /**
         * Sets the lenient toggle.
         *
         * @param lenient the lenient toggle.
         * @return {@code this} instance.
         */
        public Builder setLenient(final boolean lenient) {
            this.lenient = lenient;
            return this;
        }

    }

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    private static final String US_ASCII = StandardCharsets.US_ASCII.name();

    private static final String UTF_16BE = StandardCharsets.UTF_16BE.name();

    private static final String UTF_16LE = StandardCharsets.UTF_16LE.name();

    private static final String UTF_32BE = "UTF-32BE";

    private static final String UTF_32LE = "UTF-32LE";

    private static final String UTF_16 = StandardCharsets.UTF_16.name();

    private static final String UTF_32 = "UTF-32";

    private static final String EBCDIC = "CP1047";

    private static final ByteOrderMark[] BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE,
            ByteOrderMark.UTF_32LE };

    /** UTF_16LE and UTF_32LE have the same two starting BOM bytes. */
    private static final ByteOrderMark[] XML_GUESS_BYTES = { new ByteOrderMark(UTF_8, 0x3C, 0x3F, 0x78, 0x6D),
            new ByteOrderMark(UTF_16BE, 0x00, 0x3C, 0x00, 0x3F), new ByteOrderMark(UTF_16LE, 0x3C, 0x00, 0x3F, 0x00),
            new ByteOrderMark(UTF_32BE, 0x00, 0x00, 0x00, 0x3C, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x00, 0x78, 0x00, 0x00, 0x00, 0x6D),
            new ByteOrderMark(UTF_32LE, 0x3C, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x00, 0x78, 0x00, 0x00, 0x00, 0x6D, 0x00, 0x00, 0x00),
            new ByteOrderMark(EBCDIC, 0x4C, 0x6F, 0xA7, 0x94) };

    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=[\"']?([.[^; \"']]*)[\"']?");

    /**
     * Pattern capturing the encoding of the <a href="https://www.w3.org/TR/REC-xml/#sec-pi">{@code 'xml'} processing instruction</a>.
     * <p>
     * See also the <a href="https://www.w3.org/TR/2008/REC-xml-20081126/#NT-EncName">NT-EncName</a> XML specification.
     * </p>
     * <p>
     * Note the documented pattern is:
     * </p>
     * <pre>
     * EncName   ::=   [A-Za-z] ([A-Za-z0-9._] | '-')*
     * </pre>
     * <p>
     * However this does not match all the aliases that are supported by Java. For example, {@code '437'}, {@code 'ISO_8859-1:1987'} and
     * {@code 'ebcdic-de-273+euro'}.
     * </p>
     */
    public static final Pattern ENCODING_PATTERN = Pattern.compile(
    // @formatter:off
            "^<\\?xml\\s+"
            + "(?:version\\s*=\\s*(?:(?:\"1\\.[0-9]+\")|(?:'1.[0-9]+'))\\s+)??"
            + "encoding\\s*=\\s*"
            + "((?:\"[A-Za-z0-9][A-Za-z0-9._+:-]*\")"  // double-quoted
            +  "|(?:'[A-Za-z0-9][A-Za-z0-9._+:-]*'))", // single-quoted
            Pattern.MULTILINE);
    // @formatter:on

    private static final String RAW_EX_1 = "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch";

    private static final String RAW_EX_2 = "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM";

    private static final String HTTP_EX_1 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be null";

    private static final String HTTP_EX_2 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch";

    private static final String HTTP_EX_3 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Illegal MIME";

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the charset parameter value, {@code null} if not present, {@code null} if httpContentType is {@code null}.
     *
     * @param httpContentType the HTTP content type
     * @return The content type encoding (upcased)
     */
    static String getContentTypeEncoding(final String httpContentType) {
        String encoding = null;
        if (httpContentType != null) {
            final int i = httpContentType.indexOf(";");
            if (i > -1) {
                final String postMime = httpContentType.substring(i + 1);
                final Matcher m = CHARSET_PATTERN.matcher(postMime);
                encoding = m.find() ? m.group(1) : null;
                encoding = encoding != null ? encoding.toUpperCase(Locale.ROOT) : null;
            }
        }
        return encoding;
    }

    /**
     * Gets the MIME type or {@code null} if httpContentType is {@code null}.
     *
     * @param httpContentType the HTTP content type
     * @return The mime content type
     */
    static String getContentTypeMime(final String httpContentType) {
        String mime = null;
        if (httpContentType != null) {
            final int i = httpContentType.indexOf(";");
            mime = i >= 0 ? httpContentType.substring(0, i) : httpContentType;
            mime = mime.trim();
        }
        return mime;
    }

    /**
     * Gets the encoding declared in the <?xml encoding=...?>, {@code null} if none.
     *
     * @param inputStream InputStream to create the reader from.
     * @param guessedEnc  guessed encoding
     * @return the encoding declared in the <?xml encoding=...?>
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private static String getXmlProlog(final InputStream inputStream, final String guessedEnc) throws IOException {
        String encoding = null;
        if (guessedEnc != null) {
            final byte[] bytes = IOUtils.byteArray();
            inputStream.mark(IOUtils.DEFAULT_BUFFER_SIZE);
            int offset = 0;
            int max = IOUtils.DEFAULT_BUFFER_SIZE;
            int c = inputStream.read(bytes, offset, max);
            int firstGT = -1;
            String xmlProlog = ""; // avoid possible NPE warning (cannot happen; this just silences the warning)
            while (c != -1 && firstGT == -1 && offset < IOUtils.DEFAULT_BUFFER_SIZE) {
                offset += c;
                max -= c;
                c = inputStream.read(bytes, offset, max);
                xmlProlog = new String(bytes, 0, offset, guessedEnc);
                firstGT = xmlProlog.indexOf('>');
            }
            if (firstGT == -1) {
                if (c == -1) {
                    throw new IOException("Unexpected end of XML stream");
                }
                throw new IOException("XML prolog or ROOT element not found on first " + offset + " bytes");
            }
            final int bytesRead = offset;
            if (bytesRead > 0) {
                inputStream.reset();
                final BufferedReader bReader = new BufferedReader(new StringReader(xmlProlog.substring(0, firstGT + 1)));
                final StringBuilder prolog = new StringBuilder();
                IOConsumer.forEach(bReader.lines(), l -> prolog.append(l).append(' '));
                final Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find()) {
                    encoding = m.group(1).toUpperCase(Locale.ROOT);
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }

    /**
     * Tests if the MIME type belongs to the APPLICATION XML family.
     *
     * @param mime The mime type
     * @return true if the mime type belongs to the APPLICATION XML family, otherwise false
     */
    static boolean isAppXml(final String mime) {
        return mime != null && (mime.equals("application/xml") || mime.equals("application/xml-dtd") || mime.equals("application/xml-external-parsed-entity")
                || mime.startsWith("application/") && mime.endsWith("+xml"));
    }

    /**
     * Tests if the MIME type belongs to the TEXT XML family.
     *
     * @param mime The mime type
     * @return true if the mime type belongs to the TEXT XML family, otherwise false
     */
    static boolean isTextXml(final String mime) {
        return mime != null && (mime.equals("text/xml") || mime.equals("text/xml-external-parsed-entity") || mime.startsWith("text/") && mime.endsWith("+xml"));
    }

    private final Reader reader;

    private final String encoding;

    private final String defaultEncoding;

    /**
     * Constructs a Reader for a File.
     * <p>
     * It looks for the UTF-8 BOM first, if none sniffs the XML prolog charset, if this is also missing defaults to UTF-8.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param file File to create a Reader from.
     * @throws NullPointerException if the input is {@code null}.
     * @throws IOException          thrown if there is a problem reading the file.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamReader(final File file) throws IOException {
        this(Objects.requireNonNull(file, "file").toPath());
    }

    /**
     * Constructs a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param inputStream InputStream to create a Reader from.
     * @throws NullPointerException if the input stream is {@code null}.
     * @throws IOException          thrown if there is a problem reading the stream.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamReader(final InputStream inputStream) throws IOException {
        this(inputStream, true);
    }

    /**
     * Constructs a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * </p>
     * <p>
     * If lenient detection is indicated and the detection above fails as per specifications it then attempts the following:
     * </p>
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and tries the detection again.
     * </p>
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else 'UTF-8' is used.
     * </p>
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never thrown.
     * </p>
     *
     * @param inputStream InputStream to create a Reader from.
     * @param lenient     indicates if the charset encoding detection should be relaxed.
     * @throws NullPointerException     if the input stream is {@code null}.
     * @throws IOException              thrown if there is a problem reading the stream.
     * @throws XmlStreamReaderException thrown if the charset encoding could not be determined according to the specification.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamReader(final InputStream inputStream, final boolean lenient) throws IOException {
        this(inputStream, lenient, null);
    }

    /**
     * Constructs a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * </p>
     * <p>
     * If lenient detection is indicated and the detection above fails as per specifications it then attempts the following:
     * </p>
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and tries the detection again.
     * </p>
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else 'UTF-8' is used.
     * </p>
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never thrown.
     * </p>
     *
     * @param inputStream     InputStream to create a Reader from.
     * @param lenient         indicates if the charset encoding detection should be relaxed.
     * @param defaultEncoding The default encoding
     * @throws NullPointerException     if the input stream is {@code null}.
     * @throws IOException              thrown if there is a problem reading the stream.
     * @throws XmlStreamReaderException thrown if the charset encoding could not be determined according to the specification.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    @SuppressWarnings("resource") // InputStream is managed through a InputStreamReader in this instance.
    public XmlStreamReader(final InputStream inputStream, final boolean lenient, final String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        final BOMInputStream bom = new BOMInputStream(new BufferedInputStream(Objects.requireNonNull(inputStream, "inputStream"), IOUtils.DEFAULT_BUFFER_SIZE),
                false, BOMS);
        final BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        this.encoding = processHttpStream(bom, pis, lenient);
        this.reader = new InputStreamReader(pis, encoding);
    }

    /**
     * Constructs a Reader using an InputStream and the associated content-type header.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the content-type encoding. If there is not content-type encoding checks the XML prolog
     * encoding. If there is not XML prolog encoding uses the default encoding mandated by the content-type MIME type.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param inputStream     InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of the charset encoding.
     * @throws NullPointerException if the input stream is {@code null}.
     * @throws IOException          thrown if there is a problem reading the file.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamReader(final InputStream inputStream, final String httpContentType) throws IOException {
        this(inputStream, httpContentType, true);
    }

    /**
     * Constructs a Reader using an InputStream and the associated content-type header. This constructor is lenient regarding the encoding detection.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the content-type encoding. If there is not content-type encoding checks the XML prolog
     * encoding. If there is not XML prolog encoding uses the default encoding mandated by the content-type MIME type.
     * </p>
     * <p>
     * If lenient detection is indicated and the detection above fails as per specifications it then attempts the following:
     * </p>
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and tries the detection again.
     * </p>
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else 'UTF-8' is used.
     * </p>
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never thrown.
     * </p>
     *
     * @param inputStream     InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of the charset encoding.
     * @param lenient         indicates if the charset encoding detection should be relaxed.
     * @throws NullPointerException     if the input stream is {@code null}.
     * @throws IOException              thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not be determined according to the specification.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public XmlStreamReader(final InputStream inputStream, final String httpContentType, final boolean lenient) throws IOException {
        this(inputStream, httpContentType, lenient, null);
    }

    /**
     * Constructs a Reader using an InputStream and the associated content-type header. This constructor is lenient regarding the encoding detection.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the content-type encoding. If there is not content-type encoding checks the XML prolog
     * encoding. If there is not XML prolog encoding uses the default encoding mandated by the content-type MIME type.
     * </p>
     * <p>
     * If lenient detection is indicated and the detection above fails as per specifications it then attempts the following:
     * </p>
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and tries the detection again.
     * </p>
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * </p>
     * <p>
     * Else 'UTF-8' is used.
     * </p>
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never thrown.
     * </p>
     *
     * @param inputStream     InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of the charset encoding.
     * @param lenient         indicates if the charset encoding detection should be relaxed.
     * @param defaultEncoding The default encoding
     * @throws NullPointerException     if the input stream is {@code null}.
     * @throws IOException              thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not be determined according to the specification.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    @SuppressWarnings("resource") // InputStream is managed through a InputStreamReader in this instance.
    public XmlStreamReader(final InputStream inputStream, final String httpContentType, final boolean lenient, final String defaultEncoding)
            throws IOException {
        this.defaultEncoding = defaultEncoding;
        final BOMInputStream bom = new BOMInputStream(new BufferedInputStream(Objects.requireNonNull(inputStream, "inputStream"), IOUtils.DEFAULT_BUFFER_SIZE),
                false, BOMS);
        final BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        this.encoding = processHttpStream(bom, pis, lenient, httpContentType);
        this.reader = new InputStreamReader(pis, encoding);
    }

    /**
     * Constructs a Reader for a File.
     * <p>
     * It looks for the UTF-8 BOM first, if none sniffs the XML prolog charset, if this is also missing defaults to UTF-8.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param file File to create a Reader from.
     * @throws NullPointerException if the input is {@code null}.
     * @throws IOException          thrown if there is a problem reading the file.
     * @since 2.11.0
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    @SuppressWarnings("resource") // InputStream is managed through another reader in this instance.
    public XmlStreamReader(final Path file) throws IOException {
        this(Files.newInputStream(Objects.requireNonNull(file, "file")));
    }

    /**
     * Constructs a Reader using the InputStream of a URL.
     * <p>
     * If the URL is not of type HTTP and there is not 'content-type' header in the fetched data it uses the same logic used for Files.
     * </p>
     * <p>
     * If the URL is a HTTP Url or there is a 'content-type' header in the fetched data it uses the same logic used for an InputStream with content-type.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param url URL to create a Reader from.
     * @throws NullPointerException if the input is {@code null}.
     * @throws IOException          thrown if there is a problem reading the stream of the URL.
     */
    public XmlStreamReader(final URL url) throws IOException {
        this(Objects.requireNonNull(url, "url").openConnection(), null);
    }

    /**
     * Constructs a Reader using the InputStream of a URLConnection.
     * <p>
     * If the URLConnection is not of type HttpURLConnection and there is not 'content-type' header in the fetched data it uses the same logic used for files.
     * </p>
     * <p>
     * If the URLConnection is a HTTP Url or there is a 'content-type' header in the fetched data it uses the same logic used for an InputStream with
     * content-type.
     * </p>
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * </p>
     *
     * @param urlConnection   URLConnection to create a Reader from.
     * @param defaultEncoding The default encoding
     * @throws NullPointerException if the input is {@code null}.
     * @throws IOException          thrown if there is a problem reading the stream of the URLConnection.
     */
    public XmlStreamReader(final URLConnection urlConnection, final String defaultEncoding) throws IOException {
        Objects.requireNonNull(urlConnection, "urlConnection");
        this.defaultEncoding = defaultEncoding;
        final boolean lenient = true;
        final String contentType = urlConnection.getContentType();
        final InputStream inputStream = urlConnection.getInputStream();
        @SuppressWarnings("resource") // managed by the InputStreamReader tracked by this instance
        // @formatter:off
        final BOMInputStream bomInput = BOMInputStream.builder()
            .setInputStream(new BufferedInputStream(inputStream, IOUtils.DEFAULT_BUFFER_SIZE))
            .setInclude(false)
            .setByteOrderMarks(BOMS)
            .get();
        @SuppressWarnings("resource")
        final BOMInputStream piInput = BOMInputStream.builder()
            .setInputStream(new BufferedInputStream(bomInput, IOUtils.DEFAULT_BUFFER_SIZE))
            .setInclude(true)
            .setByteOrderMarks(XML_GUESS_BYTES)
            .get();
        // @formatter:on
        if (urlConnection instanceof HttpURLConnection || contentType != null) {
            this.encoding = processHttpStream(bomInput, piInput, lenient, contentType);
        } else {
            this.encoding = processHttpStream(bomInput, piInput, lenient);
        }
        this.reader = new InputStreamReader(piInput, encoding);
    }

    /**
     * Calculates the HTTP encoding.
     * @param bomEnc          BOM encoding
     * @param xmlGuessEnc     XML Guess encoding
     * @param xmlEnc          XML encoding
     * @param lenient         indicates if the charset encoding detection should be relaxed.
     * @param httpContentType The HTTP content type
     *
     * @return the HTTP encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    String calculateHttpEncoding(final String bomEnc, final String xmlGuessEnc, final String xmlEnc, final boolean lenient, final String httpContentType)
            throws IOException {

        // Lenient and has XML encoding
        if (lenient && xmlEnc != null) {
            return xmlEnc;
        }

        // Determine mime/encoding content types from HTTP Content Type
        final String cTMime = getContentTypeMime(httpContentType);
        final String cTEnc = getContentTypeEncoding(httpContentType);
        final boolean appXml = isAppXml(cTMime);
        final boolean textXml = isTextXml(cTMime);

        // Mime type NOT "application/xml" or "text/xml"
        if (!appXml && !textXml) {
            final String msg = MessageFormat.format(HTTP_EX_3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }

        // No content type encoding
        if (cTEnc == null) {
            if (appXml) {
                return calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
            }
            return defaultEncoding == null ? US_ASCII : defaultEncoding;
        }

        // UTF-16BE or UTF-16LE content type encoding
        if (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE)) {
            if (bomEnc != null) {
                final String msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return cTEnc;
        }

        // UTF-16 content type encoding
        if (cTEnc.equals(UTF_16)) {
            if (bomEnc != null && bomEnc.startsWith(UTF_16)) {
                return bomEnc;
            }
            final String msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }

        // UTF-32BE or UTF-132E content type encoding
        if (cTEnc.equals(UTF_32BE) || cTEnc.equals(UTF_32LE)) {
            if (bomEnc != null) {
                final String msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return cTEnc;
        }

        // UTF-32 content type encoding
        if (cTEnc.equals(UTF_32)) {
            if (bomEnc != null && bomEnc.startsWith(UTF_32)) {
                return bomEnc;
            }
            final String msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }

        return cTEnc;
    }

    /**
     * Calculate the raw encoding.
     *
     * @param bomEnc      BOM encoding
     * @param xmlGuessEnc XML Guess encoding
     * @param xmlEnc      XML encoding
     * @return the raw encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    String calculateRawEncoding(final String bomEnc, final String xmlGuessEnc, final String xmlEnc) throws IOException {

        // BOM is Null
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                return defaultEncoding == null ? UTF_8 : defaultEncoding;
            }
            if (xmlEnc.equals(UTF_16) && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc.equals(UTF_16LE))) {
                return xmlGuessEnc;
            }
            return xmlEnc;
        }

        // BOM is UTF-8
        if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }

        // BOM is UTF-16BE or UTF-16LE
        if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_16) && !xmlEnc.equals(bomEnc)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }

        // BOM is UTF-32BE or UTF-32LE
        if (bomEnc.equals(UTF_32BE) || bomEnc.equals(UTF_32LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_32) && !xmlEnc.equals(bomEnc)) {
                final String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }

        // BOM is something else
        final String msg = MessageFormat.format(RAW_EX_2, bomEnc, xmlGuessEnc, xmlEnc);
        throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
    }

    /**
     * Closes the XmlStreamReader stream.
     *
     * @throws IOException thrown if there was a problem closing the stream.
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Does lenient detection.
     *
     * @param httpContentType content-type header to use for the resolution of the charset encoding.
     * @param ex              The thrown exception
     * @return the encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private String doLenientDetection(String httpContentType, XmlStreamReaderException ex) throws IOException {
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            httpContentType = httpContentType.substring("text/html".length());
            httpContentType = "text/xml" + httpContentType;
            try {
                return calculateHttpEncoding(ex.getBomEncoding(), ex.getXmlGuessEncoding(), ex.getXmlEncoding(), true, httpContentType);
            } catch (final XmlStreamReaderException ex2) {
                ex = ex2;
            }
        }
        String encoding = ex.getXmlEncoding();
        if (encoding == null) {
            encoding = ex.getContentTypeEncoding();
        }
        if (encoding == null) {
            encoding = defaultEncoding == null ? UTF_8 : defaultEncoding;
        }
        return encoding;
    }

    /**
     * Gets the default encoding to use if none is set in HTTP content-type, XML prolog and the rules based on content-type are not adequate.
     * <p>
     * If it is {@code null} the content-type based rules are used.
     * </p>
     *
     * @return the default encoding to use.
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Gets the charset encoding of the XmlStreamReader.
     *
     * @return charset encoding.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Process the raw stream.
     *
     * @param bomInput     BOMInputStream to detect byte order marks
     * @param piInput     BOMInputStream to guess XML encoding
     * @param lenient indicates if the charset encoding detection should be relaxed.
     * @return the encoding to be used
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private String processHttpStream(final BOMInputStream bomInput, final BOMInputStream piInput, final boolean lenient) throws IOException {
        final String bomEnc = bomInput.getBOMCharsetName();
        final String xmlGuessEnc = piInput.getBOMCharsetName();
        final String xmlEnc = getXmlProlog(piInput, xmlGuessEnc);
        try {
            return calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
        } catch (final XmlStreamReaderException ex) {
            if (lenient) {
                return doLenientDetection(null, ex);
            }
            throw ex;
        }
    }

    /**
     * Processes an HTTP stream.
     *
     * @param bomInput        BOMInputStream to detect byte order marks
     * @param piInput         BOMInputStream to guess XML encoding
     * @param lenient         indicates if the charset encoding detection should be relaxed.
     * @param httpContentType The HTTP content type
     * @return the encoding to be used
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private String processHttpStream(final BOMInputStream bomInput, final BOMInputStream piInput, final boolean lenient, final String httpContentType)
            throws IOException {
        final String bomEnc = bomInput.getBOMCharsetName();
        final String xmlGuessEnc = piInput.getBOMCharsetName();
        final String xmlEnc = getXmlProlog(piInput, xmlGuessEnc);
        try {
            return calculateHttpEncoding(bomEnc, xmlGuessEnc, xmlEnc, lenient, httpContentType);
        } catch (final XmlStreamReaderException ex) {
            if (lenient) {
                return doLenientDetection(httpContentType, ex);
            }
            throw ex;
        }
    }

    /**
     * Reads the underlying reader's {@code read(char[], int, int)} method.
     *
     * @param buf    the buffer to read the characters into
     * @param offset The start offset
     * @param len    The number of bytes to read
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final char[] buf, final int offset, final int len) throws IOException {
        return reader.read(buf, offset, len);
    }

}
