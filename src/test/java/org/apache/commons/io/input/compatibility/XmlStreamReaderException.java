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
package org.apache.commons.io.input.compatibility;

import java.io.InputStream;

/**
 * The XmlStreamReaderException is thrown by the XmlStreamReader constructors if
 * the charset encoding can not be determined according to the XML 1.0
 * specification and RFC 3023.
 * <p>
 * The exception returns the unconsumed InputStream to allow the application to
 * do an alternate processing with the stream. Note that the original
 * InputStream given to the XmlStreamReader cannot be used as that one has been
 * already read.
 *
 */
public class XmlStreamReaderException extends org.apache.commons.io.input.XmlStreamReaderException {

    private static final long serialVersionUID = 1L;

    private final InputStream is;

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *
     * @param msg message describing the reason for the exception.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     * @param is the unconsumed InputStream.
     */
    public XmlStreamReaderException(final String msg, final String bomEnc,
            final String xmlGuessEnc, final String xmlEnc, final InputStream is) {
        this(msg, null, null, bomEnc, xmlGuessEnc, xmlEnc, is);
    }

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *
     * @param msg message describing the reason for the exception.
     * @param ctMime MIME type in the content-type.
     * @param ctEnc encoding in the content-type.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     * @param is the unconsumed InputStream.
     */
    public XmlStreamReaderException(final String msg, final String ctMime, final String ctEnc,
            final String bomEnc, final String xmlGuessEnc, final String xmlEnc, final InputStream is) {
        super(msg, ctMime, ctEnc, bomEnc, xmlGuessEnc, xmlEnc);
        this.is = is;
    }

    /**
     * Returns the unconsumed InputStream to allow the application to do an
     * alternate encoding detection on the InputStream.
     *
     * @return the unconsumed InputStream.
     */
    public InputStream getInputStream() {
        return is;
    }
}
