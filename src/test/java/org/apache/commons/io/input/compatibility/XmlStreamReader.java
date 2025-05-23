/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input.compatibility;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.XmlStreamWriter;

/**
 * Character stream that handles all the necessary Voodoo to figure out the
 * charset encoding of the XML document within the stream.
 * <p>
 * IMPORTANT: This class is not related in any way to the org.xml.sax.XMLReader.
 * This one IS a character stream.
 * </p>
 * <p>
 * All this has to be done without consuming characters from the stream, if not
 * the XML parser will not recognized the document as a valid XML. This is not
 * 100% true, but it's close enough (UTF-8 BOM is not handled by all parsers
 * right now, XmlStreamReader handles it and things work in all parsers).
 * </p>
 * <p>
 * The XmlStreamReader class handles the charset encoding of XML documents in
 * Files, raw streams and HTTP streams by offering a wide set of constructors.
 * </p>
 * <p>
 * By default the charset encoding detection is lenient, the constructor with
 * the lenient flag can be used for a script (following HTTP MIME and XML
 * specifications). All this is nicely explained by Mark Pilgrim in his blog, <a
 * href="https://diveintomark.org/archives/2004/02/13/xml-media-types">
 * Determining the character encoding of a feed</a>.
 * </p>
 * <p>
 * Originally developed for <a href="https://rome.dev.java.net">ROME</a> under
 * Apache License 2.0.
 * </p>
 *
 * @see XmlStreamWriter
 */
public class XmlStreamReader extends Reader {

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    private static final String US_ASCII = StandardCharsets.US_ASCII.name();

    private static final String UTF_16BE = StandardCharsets.UTF_16BE.name();

    private static final String UTF_16LE = StandardCharsets.UTF_16LE.name();

    private static final String UTF_16 = StandardCharsets.UTF_16.name();

    private static final String UTF_32BE = "UTF-32BE";

    private static final String UTF_32LE = "UTF-32LE";

    private static final String UTF_32 = "UTF-32";

    private static final String EBCDIC = "CP1047";

    private static String staticDefaultEncoding;

    private static final Pattern CHARSET_PATTERN = Pattern
            .compile("charset=[\"']?([.[^; \"']]*)[\"']?");

    public static final Pattern ENCODING_PATTERN = Pattern.compile(
            "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))",
            Pattern.MULTILINE);

    private static final MessageFormat RAW_EX_1 = new MessageFormat(
            "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch");

    private static final MessageFormat RAW_EX_2 = new MessageFormat(
            "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM");

    private static final MessageFormat HTTP_EX_1 = new MessageFormat(
            "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be null");

    private static final MessageFormat HTTP_EX_2 = new MessageFormat(
            "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch");

    private static final MessageFormat HTTP_EX_3 = new MessageFormat(
            "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Illegal MIME");

    // returns the BOM in the stream, null if not present,
    // if there was BOM the in the stream it is consumed
    static String getBOMEncoding(final BufferedInputStream is)
            throws IOException {
        String encoding = null;
        final int[] bytes = new int[3];
        is.mark(3);
        bytes[0] = is.read();
        bytes[1] = is.read();
        bytes[2] = is.read();

        if (bytes[0] == 0xFE && bytes[1] == 0xFF) {
            encoding = UTF_16BE;
            is.reset();
            is.read();
            is.read();
        } else if (bytes[0] == 0xFF && bytes[1] == 0xFE) {
            encoding = UTF_16LE;
            is.reset();
            is.read();
            is.read();
        } else if (bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF) {
            encoding = UTF_8;
        } else {
            is.reset();
        }
        return encoding;
    }

    // returns charset parameter value, null if not present, null if
    // httpContentType is null
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

    // returns MIME type or null if httpContentType is null
    static String getContentTypeMime(final String httpContentType) {
        String mime = null;
        if (httpContentType != null) {
            final int i = httpContentType.indexOf(";");
            mime = (i == -1 ? httpContentType : httpContentType.substring(0,
                    i)).trim();
        }
        return mime;
    }

    /**
     * Returns the default encoding to use if none is set in HTTP content-type,
     * XML prolog and the rules based on content-type are not adequate.
     * <p>
     * If it is null the content-type based rules are used.
     *
     * @return the default encoding to use.
     */
    public static String getDefaultEncoding() {
        return staticDefaultEncoding;
    }

    // returns the best guess for the encoding by looking the first bytes of the
    // stream, '<?'
    private static String getXMLGuessEncoding(final BufferedInputStream is)
            throws IOException {
        String encoding = null;
        final int[] bytes = new int[4];
        is.mark(4);
        bytes[0] = is.read();
        bytes[1] = is.read();
        bytes[2] = is.read();
        bytes[3] = is.read();
        is.reset();

        if (bytes[0] == 0x00 && bytes[1] == 0x3C && bytes[2] == 0x00
                && bytes[3] == 0x3F) {
            encoding = UTF_16BE;
        } else if (bytes[0] == 0x3C && bytes[1] == 0x00 && bytes[2] == 0x3F
                && bytes[3] == 0x00) {
            encoding = UTF_16LE;
        } else if (bytes[0] == 0x3C && bytes[1] == 0x3F && bytes[2] == 0x78
                && bytes[3] == 0x6D) {
            encoding = UTF_8;
        } else if (bytes[0] == 0x4C && bytes[1] == 0x6F && bytes[2] == 0xA7
                && bytes[3] == 0x94) {
            encoding = EBCDIC;
        }
        return encoding;
    }

    // returns the encoding declared in the <?xml encoding=...?>, null if none
    private static String getXmlProlog(final BufferedInputStream is, final String guessedEnc)
            throws IOException {
        String encoding = null;
        if (guessedEnc != null) {
            final byte[] bytes = IOUtils.byteArray();
            is.mark(IOUtils.DEFAULT_BUFFER_SIZE);
            int offset = 0;
            int max = IOUtils.DEFAULT_BUFFER_SIZE;
            int c = is.read(bytes, offset, max);
            int firstGT = -1;
            String xmlProlog = ""; // avoid possible NPE warning (cannot happen; this just silences the warning)
            while (c != -1 && firstGT == -1 && offset < IOUtils.DEFAULT_BUFFER_SIZE) {
                offset += c;
                max -= c;
                c = is.read(bytes, offset, max);
                xmlProlog = new String(bytes, 0, offset, guessedEnc);
                firstGT = xmlProlog.indexOf('>');
            }
            if (firstGT == -1) {
                if (c == -1) {
                    throw new IOException("Unexpected end of XML stream");
                }
                throw new IOException(
                        "XML prolog or ROOT element not found on first "
                                + offset + " bytes");
            }
            final int bytesRead = offset;
            if (bytesRead > 0) {
                is.reset();
                final BufferedReader bReader = new BufferedReader(new StringReader(
                        xmlProlog.substring(0, firstGT + 1)));
                final StringBuilder prolog = new StringBuilder();
                String line;
                while ((line = bReader.readLine()) != null) {
                    prolog.append(line);
                }
                final Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find()) {
                    encoding = m.group(1).toUpperCase(Locale.ROOT);
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }

    // indicates if the MIME type belongs to the APPLICATION XML family
    static boolean isAppXml(final String mime) {
        return mime != null
                && (mime.equals("application/xml")
                        || mime.equals("application/xml-dtd")
                        || mime
                                .equals("application/xml-external-parsed-entity") || mime
                        .startsWith("application/") && mime.endsWith("+xml"));
    }

    // indicates if the MIME type belongs to the TEXT XML family
    static boolean isTextXml(final String mime) {
        return mime != null
                && (mime.equals("text/xml")
                        || mime.equals("text/xml-external-parsed-entity") || mime
                        .startsWith("text/") && mime.endsWith("+xml"));
    }

    /**
     * Sets the default encoding to use if none is set in HTTP content-type, XML
     * prolog and the rules based on content-type are not adequate.
     * <p>
     * If it is set to null the content-type based rules are used.
     * <p>
     * By default it is null.
     *
     * @param encoding charset encoding to default to.
     */
    public static void setDefaultEncoding(final String encoding) {
        staticDefaultEncoding = encoding;
    }

    private Reader reader;

    private String encoding;

    private final String defaultEncoding;

    /**
     * Creates a Reader for a File.
     * <p>
     * It looks for the UTF-8 BOM first, if none sniffs the XML prolog charset,
     * if this is also missing defaults to UTF-8.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param file File to create a Reader from.
     * @throws IOException thrown if there is a problem reading the file.
     */
    @SuppressWarnings("resource") // FileInputStream is closed when this closed when this object is closed.
    public XmlStreamReader(final File file) throws IOException {
        this(Files.newInputStream(file.toPath()));
    }

    /**
     * Creates a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param inputStream InputStream to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream.
     */
    public XmlStreamReader(final InputStream inputStream) throws IOException {
        this(inputStream, true);
    }

    /**
     * Creates a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * <p>
     * If lenient detection is indicated and the detection above fails as per
     * specifications it then attempts the following:
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and
     * tries the detection again.
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * <p>
     * Else 'UTF-8' is used.
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never
     * thrown.
     *
     * @param inputStream InputStream to create a Reader from.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the stream.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specification.
     */
    public XmlStreamReader(final InputStream inputStream, final boolean lenient) throws IOException,
            XmlStreamReaderException {
        defaultEncoding = staticDefaultEncoding;
        try {
            doRawStream(inputStream);
        } catch (final XmlStreamReaderException ex) {
            if (!lenient) {
                throw ex;
            }
            doLenientDetection(null, ex);
        }
    }

    /**
     * Creates a Reader using an InputStream and the associated content-type
     * header.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param inputStream InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @throws IOException thrown if there is a problem reading the file.
     */
    public XmlStreamReader(final InputStream inputStream, final String httpContentType)
            throws IOException {
        this(inputStream, httpContentType, true);
    }

    /**
     * Creates a Reader using an InputStream and the associated content-type
     * header. This constructor is lenient regarding the encoding detection.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     * <p>
     * If lenient detection is indicated and the detection above fails as per
     * specifications it then attempts the following:
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and
     * tries the detection again.
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * <p>
     * Else 'UTF-8' is used.
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never
     * thrown.
     *
     * @param inputStream InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specification.
     */
    public XmlStreamReader(final InputStream inputStream, final String httpContentType,
            final boolean lenient) throws IOException, XmlStreamReaderException {
        this(inputStream, httpContentType, lenient, null);
    }

    /**
     * Creates a Reader using an InputStream and the associated content-type
     * header. This constructor is lenient regarding the encoding detection.
     * <p>
     * First it checks if the stream has BOM. If there is not BOM checks the
     * content-type encoding. If there is not content-type encoding checks the
     * XML prolog encoding. If there is not XML prolog encoding uses the default
     * encoding mandated by the content-type MIME type.
     * <p>
     * If lenient detection is indicated and the detection above fails as per
     * specifications it then attempts the following:
     * <p>
     * If the content type was 'text/html' it replaces it with 'text/xml' and
     * tries the detection again.
     * <p>
     * Else if the XML prolog had a charset encoding that encoding is used.
     * <p>
     * Else if the content type had a charset encoding that encoding is used.
     * <p>
     * Else 'UTF-8' is used.
     * <p>
     * If lenient detection is indicated an XmlStreamReaderException is never
     * thrown.
     *
     * @param inputStream InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @param defaultEncoding the default encoding to use
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specification.
     */
    public XmlStreamReader(final InputStream inputStream, final String httpContentType,
            final boolean lenient, final String defaultEncoding) throws IOException,
            XmlStreamReaderException {
        this.defaultEncoding = defaultEncoding == null ? staticDefaultEncoding
                : defaultEncoding;
        try {
            doHttpStream(inputStream, httpContentType, lenient);
        } catch (final XmlStreamReaderException ex) {
            if (!lenient) {
                throw ex;
            }
            doLenientDetection(httpContentType, ex);
        }
    }

    /**
     * Creates a Reader using the InputStream of a URL.
     * <p>
     * If the URL is not of type HTTP and there is not 'content-type' header in
     * the fetched data it uses the same logic used for Files.
     * <p>
     * If the URL is a HTTP Url or there is a 'content-type' header in the
     * fetched data it uses the same logic used for an InputStream with
     * content-type.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param url URL to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream of
     *         the URL.
     */
    public XmlStreamReader(final URL url) throws IOException {
        // TODO URLConnection leak.
        this(url.openConnection());
    }

    /**
     * Creates a Reader using the InputStream of a URLConnection.
     * <p>
     * If the URLConnection is not of type HttpURLConnection and there is not
     * 'content-type' header in the fetched data it uses the same logic used for
     * files.
     * <p>
     * If the URLConnection is a HTTP Url or there is a 'content-type' header in
     * the fetched data it uses the same logic used for an InputStream with
     * content-type.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param conn URLConnection to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream of
     *         the URLConnection.
     */
    public XmlStreamReader(final URLConnection conn) throws IOException {
        defaultEncoding = staticDefaultEncoding;
        final boolean lenient = true;
        if (conn instanceof HttpURLConnection || conn.getContentType() != null) {
            try {
                doHttpStream(conn.getInputStream(), conn.getContentType(),
                        lenient);
            } catch (final XmlStreamReaderException ex) {
                doLenientDetection(conn.getContentType(), ex);
            }
        } else {
            try {
                doRawStream(conn.getInputStream());
            } catch (final XmlStreamReaderException ex) {
                doLenientDetection(null, ex);
            }
        }
    }

    // InputStream is passed for XmlStreamReaderException creation only
    String calculateHttpEncoding(final String cTMime, final String cTEnc,
            final String bomEnc, final String xmlGuessEnc, final String xmlEnc, final InputStream is,
            final boolean lenient) throws IOException {
        final String encoding;
        if (lenient && xmlEnc != null) {
            encoding = xmlEnc;
        } else {
            final boolean appXml = isAppXml(cTMime);
            final boolean textXml = isTextXml(cTMime);
            if (!appXml && !textXml) {
                throw new XmlStreamReaderException(HTTP_EX_3
                        .format(new Object[] { cTMime, cTEnc, bomEnc,
                                xmlGuessEnc, xmlEnc }), cTMime, cTEnc, bomEnc,
                        xmlGuessEnc, xmlEnc, is);
            }
            if (cTEnc == null) {
                if (appXml) {
                    encoding = calculateRawEncoding(bomEnc, xmlGuessEnc,
                            xmlEnc, is);
                } else {
                    encoding = defaultEncoding == null ? US_ASCII
                            : defaultEncoding;
                }
            } else if (bomEnc != null
                    && (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE))) {
                throw new XmlStreamReaderException(HTTP_EX_1
                        .format(new Object[] { cTMime, cTEnc, bomEnc,
                                xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                        bomEnc, xmlGuessEnc, xmlEnc, is);
            } else if (cTEnc.equals(UTF_16)) {
                if (bomEnc == null || !bomEnc.startsWith(UTF_16)) {
                    throw new XmlStreamReaderException(HTTP_EX_2
                            .format(new Object[] { cTMime, cTEnc, bomEnc,
                                    xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                            bomEnc, xmlGuessEnc, xmlEnc, is);
                }
                encoding = bomEnc;
            } else if (bomEnc != null
                    && (cTEnc.equals(UTF_32BE) || cTEnc.equals(UTF_32LE))) {
                throw new XmlStreamReaderException(HTTP_EX_1
                        .format(new Object[] { cTMime, cTEnc, bomEnc,
                                xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                        bomEnc, xmlGuessEnc, xmlEnc, is);
            } else if (cTEnc.equals(UTF_32)) {
                if (bomEnc == null || !bomEnc.startsWith(UTF_32)) {
                    throw new XmlStreamReaderException(HTTP_EX_2
                            .format(new Object[] { cTMime, cTEnc, bomEnc,
                                    xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                            bomEnc, xmlGuessEnc, xmlEnc, is);
                }
                encoding = bomEnc;
            } else {
                encoding = cTEnc;
            }
        }
        return encoding;
    }

    // InputStream is passed for XmlStreamReaderException creation only
    String calculateRawEncoding(final String bomEnc, final String xmlGuessEnc,
            final String xmlEnc, final InputStream is) throws IOException {
        final String encoding;
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                encoding = defaultEncoding == null ? UTF_8 : defaultEncoding;
            } else if (xmlEnc.equals(UTF_16)
                    && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc
                            .equals(UTF_16LE))) {
                encoding = xmlGuessEnc;
            } else if (xmlEnc.equals(UTF_32)
                    && (xmlGuessEnc.equals(UTF_32BE) || xmlGuessEnc
                            .equals(UTF_32LE))) {
                encoding = xmlGuessEnc;
            } else {
                encoding = xmlEnc;
            }
        } else if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                throw new XmlStreamReaderException(RAW_EX_1
                        .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                        bomEnc, xmlGuessEnc, xmlEnc, is);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                throw new XmlStreamReaderException(RAW_EX_1
                        .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                        bomEnc, xmlGuessEnc, xmlEnc, is);
            }
            encoding = UTF_8;
        } else {
            if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
                if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                    throw new XmlStreamReaderException(RAW_EX_1.format(new Object[] { bomEnc,
                            xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
                }
                if (xmlEnc != null && !xmlEnc.equals(UTF_16)
                        && !xmlEnc.equals(bomEnc)) {
                    throw new XmlStreamReaderException(RAW_EX_1
                            .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                            bomEnc, xmlGuessEnc, xmlEnc, is);
                }
            } else if (bomEnc.equals(UTF_32BE) || bomEnc.equals(UTF_32LE)) {
                if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                    throw new XmlStreamReaderException(RAW_EX_1.format(new Object[] { bomEnc,
                            xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc, xmlEnc, is);
                }
                if (xmlEnc != null && !xmlEnc.equals(UTF_32)
                        && !xmlEnc.equals(bomEnc)) {
                    throw new XmlStreamReaderException(RAW_EX_1
                            .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                            bomEnc, xmlGuessEnc, xmlEnc, is);
                }
            } else {
                throw new XmlStreamReaderException(RAW_EX_2.format(new Object[] {
                        bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc,
                        xmlEnc, is);
            }
            encoding = bomEnc;
        }
        return encoding;
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

    private void doHttpStream(final InputStream inputStream, final String httpContentType,
            final boolean lenient) throws IOException {
        final BufferedInputStream pis = new BufferedInputStream(inputStream, IOUtils.DEFAULT_BUFFER_SIZE);
        final String cTMime = getContentTypeMime(httpContentType);
        final String cTEnc = getContentTypeEncoding(httpContentType);
        final String bomEnc = getBOMEncoding(pis);
        final String xmlGuessEnc = getXMLGuessEncoding(pis);
        final String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        final String encoding = calculateHttpEncoding(cTMime, cTEnc, bomEnc,
                xmlGuessEnc, xmlEnc, pis, lenient);
        prepareReader(pis, encoding);
    }

    private void doLenientDetection(String httpContentType,
            XmlStreamReaderException ex) throws IOException {
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            httpContentType = httpContentType.substring("text/html"
                    .length());
            httpContentType = "text/xml" + httpContentType;
            try {
                doHttpStream(ex.getInputStream(), httpContentType, true);
                ex = null;
            } catch (final XmlStreamReaderException ex2) {
                ex = ex2;
            }
        }
        if (ex != null) {
            String encoding = ex.getXmlEncoding();
            if (encoding == null) {
                encoding = ex.getContentTypeEncoding();
            }
            if (encoding == null) {
                encoding = defaultEncoding == null ? UTF_8 : defaultEncoding;
            }
            prepareReader(ex.getInputStream(), encoding);
        }
    }

    private void doRawStream(final InputStream inputStream)
            throws IOException {
        final BufferedInputStream pis = new BufferedInputStream(inputStream, IOUtils.DEFAULT_BUFFER_SIZE);
        final String bomEnc = getBOMEncoding(pis);
        final String xmlGuessEnc = getXMLGuessEncoding(pis);
        final String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        final String encoding = calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc, pis);
        prepareReader(pis, encoding);
    }

    /**
     * Returns the charset encoding of the XmlStreamReader.
     *
     * @return charset encoding.
     */
    public String getEncoding() {
        return encoding;
    }

    private void prepareReader(final InputStream inputStream, final String encoding)
            throws IOException {
        reader = new InputStreamReader(inputStream, encoding);
        this.encoding = encoding;
    }

    @Override
    public int read(final char[] buf, final int offset, final int len) throws IOException {
        return reader.read(buf, offset, len);
    }

}
