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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.MessageFormat;

import org.apache.commons.io.ByteOrderMark;

/**
 * Character stream that handles all the necessary Voodo to figure out the
 * charset encoding of the XML document within the stream.
 * <p>
 * IMPORTANT: This class is not related in any way to the org.xml.sax.XMLReader.
 * This one IS a character stream.
 * <p>
 * All this has to be done without consuming characters from the stream, if not
 * the XML parser will not recognized the document as a valid XML. This is not
 * 100% true, but it's close enough (UTF-8 BOM is not handled by all parsers
 * right now, XmlStreamReader handles it and things work in all parsers).
 * <p>
 * The XmlStreamReader class handles the charset encoding of XML documents in
 * Files, raw streams and HTTP streams by offering a wide set of constructors.
 * <p>
 * By default the charset encoding detection is lenient, the constructor with
 * the lenient flag can be used for an script (following HTTP MIME and XML
 * specifications). All this is nicely explained by Mark Pilgrim in his blog, <a
 * href="http://diveintomark.org/archives/2004/02/13/xml-media-types">
 * Determining the character encoding of a feed</a>.
 * <p>
 * Originally developed for <a href="http://rome.dev.java.net">ROME</a> under
 * Apache License 2.0.
 *
 * @author Alejandro Abdelnur
 * @version $Id$
 * @see org.apache.commons.io.output.XmlStreamWriter
 * @since Commons IO 2.0
 */
public class XmlStreamReader extends Reader {
    private static final int BUFFER_SIZE = 4096;

    private static final String UTF_8 = "UTF-8";

    private static final String US_ASCII = "US-ASCII";

    private static final String UTF_16BE = "UTF-16BE";

    private static final String UTF_16LE = "UTF-16LE";

    private static final String UTF_16 = "UTF-16";

    private static final String EBCDIC = "CP1047";

    private static final ByteOrderMark XML_UTF_8    = new ByteOrderMark(UTF_8,    0x3C, 0x3F, 0x78, 0x6D);
    private static final ByteOrderMark XML_UTF_16BE = new ByteOrderMark(UTF_16BE, 0x00, 0x3C, 0x00, 0x3F);
    private static final ByteOrderMark XML_UTF_16LE = new ByteOrderMark(UTF_16LE, 0x3C, 0x00, 0x3F, 0x00);
    private static final ByteOrderMark XML_EBCDIC   = new ByteOrderMark(EBCDIC,   0x4C, 0x6F, 0xA7, 0x94);


    private static String staticDefaultEncoding = null;

    private Reader reader;

    private String encoding;

    private String defaultEncoding;

    /**
     * Sets the default encoding to use if none is set in HTTP content-type, XML
     * prolog and the rules based on content-type are not adequate.
     * <p>
     * If it is set to NULL the content-type based rules are used.
     * <p>
     * By default it is NULL.
     *
     * @param encoding charset encoding to default to.
     */
    public static void setDefaultEncoding(String encoding) {
        staticDefaultEncoding = encoding;
    }

    /**
     * Returns the default encoding to use if none is set in HTTP content-type,
     * XML prolog and the rules based on content-type are not adequate.
     * <p>
     * If it is NULL the content-type based rules are used.
     *
     * @return the default encoding to use.
     */
    public static String getDefaultEncoding() {
        return staticDefaultEncoding;
    }

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
    public XmlStreamReader(File file) throws IOException {
        this(new FileInputStream(file));
    }

    /**
     * Creates a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with
     * the lenient parameter for details.
     *
     * @param is InputStream to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream.
     */
    public XmlStreamReader(InputStream is) throws IOException {
        this(is, true);
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
     * @param is InputStream to create a Reader from.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the stream.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specs.
     */
    public XmlStreamReader(InputStream is, boolean lenient) throws IOException,
            XmlStreamReaderException {
        defaultEncoding = staticDefaultEncoding;
        try {
            doRawStream(is, lenient);
        } catch (XmlStreamReaderException ex) {
            if (!lenient) {
                throw ex;
            } else {
                doLenientDetection(null, is, ex);
            }
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
    public XmlStreamReader(URL url) throws IOException {
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
    public XmlStreamReader(URLConnection conn) throws IOException {
        defaultEncoding = staticDefaultEncoding;
        boolean lenient = true;
        InputStream is = conn.getInputStream();
        String contentType = conn.getContentType();
        if (conn instanceof HttpURLConnection || contentType != null) {
            try {
                doHttpStream(is, contentType, lenient);
            } catch (XmlStreamReaderException ex) {
                doLenientDetection(contentType, is, ex);
            }
        } else {
            try {
                doRawStream(is, lenient);
            } catch (XmlStreamReaderException ex) {
                doLenientDetection(null, is, ex);
            }
        }
    }

    /**
     * Creates a Reader using an InputStream an the associated content-type
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
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @throws IOException thrown if there is a problem reading the file.
     */
    public XmlStreamReader(InputStream is, String httpContentType)
            throws IOException {
        this(is, httpContentType, true);
    }

    /**
     * Creates a Reader using an InputStream an the associated content-type
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
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @param defaultEncoding The default encoding
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specs.
     */
    public XmlStreamReader(InputStream is, String httpContentType,
            boolean lenient, String defaultEncoding) throws IOException,
            XmlStreamReaderException {
        this.defaultEncoding = (defaultEncoding == null) ? staticDefaultEncoding
                : defaultEncoding;
        try {
            doHttpStream(is, httpContentType, lenient);
        } catch (XmlStreamReaderException ex) {
            if (!lenient) {
                throw ex;
            } else {
                doLenientDetection(httpContentType, is, ex);
            }
        }
    }

    /**
     * Creates a Reader using an InputStream an the associated content-type
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
     * @param is InputStream to create the reader from.
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the file.
     * @throws XmlStreamReaderException thrown if the charset encoding could not
     *         be determined according to the specs.
     */
    public XmlStreamReader(InputStream is, String httpContentType,
            boolean lenient) throws IOException, XmlStreamReaderException {
        this(is, httpContentType, lenient, null);
    }

    /**
     * Do lenient detection.
     *
     * @param httpContentType content-type header to use for the resolution of
     *        the charset encoding.
     * @param is the unconsumed InputStream
     * @param ex The thrown exception
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private void doLenientDetection(String httpContentType, InputStream is,
            XmlStreamReaderException ex) throws IOException {
        if (httpContentType != null) {
            if (httpContentType.startsWith("text/html")) {
                httpContentType = httpContentType.substring("text/html"
                        .length());
                httpContentType = "text/xml" + httpContentType;
                try {
                    doHttpStream(is, httpContentType, true);
                    ex = null;
                } catch (XmlStreamReaderException ex2) {
                    ex = ex2;
                }
            }
        }
        if (ex != null) {
            String encoding = ex.getXmlEncoding();
            if (encoding == null) {
                encoding = ex.getContentTypeEncoding();
            }
            if (encoding == null) {
                encoding = (defaultEncoding == null) ? UTF_8 : defaultEncoding;
            }
            prepareReader(is, encoding);
        }
    }

    /**
     * Returns the charset encoding of the XmlStreamReader.
     *
     * @return charset encoding.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Invokes the underlying reader's <code>read(char[], int, int)</code> method.
     * @param buf the buffer to read the characters into
     * @param offset The start offset
     * @param len The number of bytes to read
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        return reader.read(buf, offset, len);
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
     * Process the raw stream.
     *
     * @param is InputStream to create the reader from.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private void doRawStream(InputStream is, boolean lenient)
            throws IOException {
        BOMInputStream bom = createBomStream(new BufferedInputStream(is, BUFFER_SIZE)); 
        BOMInputStream pis = createXmlStream(bom);
        String bomEnc      = (bom.hasBOM() ? bom.getBOM().getCharsetName() : null);
        String xmlGuessEnc = (pis.hasBOM() ? pis.getBOM().getCharsetName() : null);
        String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        String encoding = calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc, pis);
        prepareReader(pis, encoding);
    }

    /**
     * Process a HTTP stream.
     *
     * @param is InputStream to create the reader from.
     * @param httpContentType The HTTP content type
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private void doHttpStream(InputStream is, String httpContentType,
            boolean lenient) throws IOException {
        BOMInputStream bom = createBomStream(new BufferedInputStream(is, BUFFER_SIZE)); 
        BOMInputStream pis = createXmlStream(bom);
        String cTMime = getContentTypeMime(httpContentType);
        String cTEnc = getContentTypeEncoding(httpContentType);
        String bomEnc      = (bom.hasBOM() ? bom.getBOM().getCharsetName() : null);
        String xmlGuessEnc = (pis.hasBOM() ? pis.getBOM().getCharsetName() : null);
        String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        String encoding = calculateHttpEncoding(cTMime, cTEnc, bomEnc,
                xmlGuessEnc, xmlEnc, pis, lenient);
        prepareReader(pis, encoding);
    }

    /**
     * Create a stream to detect UTF-8, UTF-16BE and UTF-16LE BOMs and consume them.
     *
     * @param delegate The delegate input stream
     * @return BOM detection stream
     */
    private BOMInputStream createBomStream(InputStream delegate) {
        BOMInputStream bis =
            new BOMInputStream(delegate, false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE);
        return bis;
    }

    /**
     * Create a stream to Guess UTF-8, UTF-16BE, UTF-16LE and EBCDIC encodings
     * in XML streams.
     *
     * @param delegate The delegate input stream
     * @return XML encoding detection stream
     */
    private BOMInputStream createXmlStream(InputStream delegate) {
        BOMInputStream bis =
            new BOMInputStream(delegate, true, XML_UTF_8, XML_UTF_16BE, XML_UTF_16LE, XML_EBCDIC);
        return bis;
    }

    /**
     * Prepare the underlying reader.
     *
     * @param is InputStream to create the reader from.
     * @param encoding The encoding
     * @throws IOException thrown if there is a problem creating the reader.
     */
    private void prepareReader(InputStream is, String encoding)
            throws IOException {
        reader = new InputStreamReader(is, encoding);
        this.encoding = encoding;
    }

    /**
     * Calculate the raw encoding.
     *
     * @param bomEnc BOM encoding
     * @param xmlGuessEnc XML Guess encoding
     * @param xmlEnc XML encoding
     * @param is InputStream to create the reader from.
     * @return the raw encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private String calculateRawEncoding(String bomEnc, String xmlGuessEnc,
            String xmlEnc, InputStream is) throws IOException {
        String encoding;
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                encoding = (defaultEncoding == null) ? UTF_8 : defaultEncoding;
            } else if (xmlEnc.equals(UTF_16)
                    && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc
                            .equals(UTF_16LE))) {
                encoding = xmlGuessEnc;
            } else {
                encoding = xmlEnc;
            }
        } else if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                throw new XmlStreamReaderException(RAW_EX_1
                        .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                        bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                throw new XmlStreamReaderException(RAW_EX_1
                        .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                        bomEnc, xmlGuessEnc, xmlEnc);
            }
            encoding = UTF_8;
        } else if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                throw new IOException(RAW_EX_1.format(new Object[] { bomEnc,
                        xmlGuessEnc, xmlEnc }));
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_16)
                    && !xmlEnc.equals(bomEnc)) {
                throw new XmlStreamReaderException(RAW_EX_1
                        .format(new Object[] { bomEnc, xmlGuessEnc, xmlEnc }),
                        bomEnc, xmlGuessEnc, xmlEnc);
            }
            encoding = bomEnc;
        } else {
            throw new XmlStreamReaderException(RAW_EX_2.format(new Object[] {
                    bomEnc, xmlGuessEnc, xmlEnc }), bomEnc, xmlGuessEnc,
                    xmlEnc);
        }
        return encoding;
    }


    /**
     * Calculate the HTTP encoding.
     *
     * @param cTMime Mime Content Type
     * @param cTEnc the content type encoding
     * @param bomEnc BOM encoding
     * @param xmlGuessEnc XML Guess encoding
     * @param xmlEnc XML encoding
     * @param is InputStream to create the reader from.
     * @param lenient indicates if the charset encoding detection should be
     *        relaxed.
     * @return the HTTP encoding
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private String calculateHttpEncoding(String cTMime, String cTEnc,
            String bomEnc, String xmlGuessEnc, String xmlEnc, InputStream is,
            boolean lenient) throws IOException {
        String encoding;
        if (lenient & xmlEnc != null) {
            encoding = xmlEnc;
        } else {
            boolean appXml = isAppXml(cTMime);
            boolean textXml = isTextXml(cTMime);
            if (appXml || textXml) {
                if (cTEnc == null) {
                    if (appXml) {
                        encoding = calculateRawEncoding(bomEnc, xmlGuessEnc,
                                xmlEnc, is);
                    } else {
                        encoding = (defaultEncoding == null) ? US_ASCII
                                : defaultEncoding;
                    }
                } else if (bomEnc != null
                        && (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE))) {
                    throw new XmlStreamReaderException(HTTP_EX_1
                            .format(new Object[] { cTMime, cTEnc, bomEnc,
                                    xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                            bomEnc, xmlGuessEnc, xmlEnc);
                } else if (cTEnc.equals(UTF_16)) {
                    if (bomEnc != null && bomEnc.startsWith(UTF_16)) {
                        encoding = bomEnc;
                    } else {
                        throw new XmlStreamReaderException(HTTP_EX_2
                                .format(new Object[] { cTMime, cTEnc, bomEnc,
                                        xmlGuessEnc, xmlEnc }), cTMime, cTEnc,
                                bomEnc, xmlGuessEnc, xmlEnc);
                    }
                } else {
                    encoding = cTEnc;
                }
            } else {
                throw new XmlStreamReaderException(HTTP_EX_3
                        .format(new Object[] { cTMime, cTEnc, bomEnc,
                                xmlGuessEnc, xmlEnc }), cTMime, cTEnc, bomEnc,
                        xmlGuessEnc, xmlEnc);
            }
        }
        return encoding;
    }

    /**
     * Returns MIME type or NULL if httpContentType is NULL.
     *
     * @param httpContentType the HTTP content type
     * @return The mime content type
     */
    private static String getContentTypeMime(String httpContentType) {
        String mime = null;
        if (httpContentType != null) {
            int i = httpContentType.indexOf(";");
            mime = ((i == -1) ? httpContentType : httpContentType.substring(0,
                    i)).trim();
        }
        return mime;
    }

    private static final Pattern CHARSET_PATTERN = Pattern
            .compile("charset=[\"']?([.[^; \"']]*)[\"']?");

    /**
     * Returns charset parameter value, NULL if not present, NULL if
     * httpContentType is NULL.
     *
     * @param httpContentType the HTTP content type
     * @return The content type encoding
     */
    private static String getContentTypeEncoding(String httpContentType) {
        String encoding = null;
        if (httpContentType != null) {
            int i = httpContentType.indexOf(";");
            if (i > -1) {
                String postMime = httpContentType.substring(i + 1);
                Matcher m = CHARSET_PATTERN.matcher(postMime);
                encoding = (m.find()) ? m.group(1) : null;
                encoding = (encoding != null) ? encoding.toUpperCase() : null;
            }
        }
        return encoding;
    }

    public static final Pattern ENCODING_PATTERN = Pattern.compile(
            "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))",
            Pattern.MULTILINE);

    /**
     * Returns the encoding declared in the <?xml encoding=...?>, NULL if none.
     *
     * @param is InputStream to create the reader from.
     * @param guessedEnc guessed encoding
     * @return the encoding declared in the <?xml encoding=...?>
     * @throws IOException thrown if there is a problem reading the stream.
     */
    private static String getXmlProlog(InputStream is, String guessedEnc)
            throws IOException {
        String encoding = null;
        if (guessedEnc != null) {
            byte[] bytes = new byte[BUFFER_SIZE];
            is.mark(BUFFER_SIZE);
            int offset = 0;
            int max = BUFFER_SIZE;
            int c = is.read(bytes, offset, max);
            int firstGT = -1;
            String xmlProlog = null;
            while (c != -1 && firstGT == -1 && offset < BUFFER_SIZE) {
                offset += c;
                max -= c;
                c = is.read(bytes, offset, max);
                xmlProlog = new String(bytes, 0, offset, guessedEnc);
                firstGT = xmlProlog.indexOf('>');
            }
            if (firstGT == -1) {
                if (c == -1) {
                    throw new IOException("Unexpected end of XML stream");
                } else {
                    throw new IOException(
                            "XML prolog or ROOT element not found on first "
                                    + offset + " bytes");
                }
            }
            int bytesRead = offset;
            if (bytesRead > 0) {
                is.reset();
                BufferedReader bReader = new BufferedReader(new StringReader(
                        xmlProlog.substring(0, firstGT + 1)));
                StringBuffer prolog = new StringBuffer();
                String line = bReader.readLine();
                while (line != null) {
                    prolog.append(line);
                    line = bReader.readLine();
                }
                Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find()) {
                    encoding = m.group(1).toUpperCase();
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }

    /**
     * Indicates if the MIME type belongs to the APPLICATION XML family.
     * 
     * @param mime The mime type
     * @return true if the mime type belongs to the APPLICATION XML family,
     * otherwise false
     */
    private static boolean isAppXml(String mime) {
        return mime != null
                && (mime.equals("application/xml")
                        || mime.equals("application/xml-dtd")
                        || mime
                                .equals("application/xml-external-parsed-entity") || (mime
                        .startsWith("application/") && mime.endsWith("+xml")));
    }

    /**
     * Indicates if the MIME type belongs to the TEXT XML family.
     * 
     * @param mime The mime type
     * @return true if the mime type belongs to the TEXT XML family,
     * otherwise false
     */
    private static boolean isTextXml(String mime) {
        return mime != null
                && (mime.equals("text/xml")
                        || mime.equals("text/xml-external-parsed-entity") || (mime
                        .startsWith("text/") && mime.endsWith("+xml")));
    }

    private static final MessageFormat RAW_EX_1 = new MessageFormat(
        "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch");

    private static final MessageFormat RAW_EX_2 = new MessageFormat(
        "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM");

    private static final MessageFormat HTTP_EX_1 = new MessageFormat(
        "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be NULL");

    private static final MessageFormat HTTP_EX_2 = new MessageFormat(
        "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch");

    private static final MessageFormat HTTP_EX_3 = new MessageFormat(
        "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Invalid MIME");

}
