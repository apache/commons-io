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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class is an example for using an {@link ObservableInputStream}. It
 * creates its own {@link Observer}, which calculates a checksum using a
 * MessageDigest, for example an MD5 sum.
 * {@em Note}: Neither {@link ObservableInputStream}, nor {@link MessageDigest},
 * are thread safe. So is {@link MessageDigestCalculatingInputStream}.
 */
public class MessageDigestCalculatingInputStream extends ObservableInputStream {
    public static class MessageDigestMaintainingObserver extends Observer {
        private final MessageDigest md;

        public MessageDigestMaintainingObserver(MessageDigest pMd) {
            md = pMd;
        }

        @Override
        void data(int pByte) throws IOException {
            md.update((byte) pByte);
        }

        @Override
        void data(byte[] pBuffer, int pOffset, int pLength) throws IOException {
            md.update(pBuffer, pOffset, pLength);
        }
    }

    private final MessageDigest messageDigest;

    /** Creates a new instance, which calculates a signature on the given stream,
     * using the given {@link MessageDigest}.
     */
    public MessageDigestCalculatingInputStream(InputStream pStream, MessageDigest pDigest) {
        super(pStream);
        messageDigest = pDigest;
        add(new MessageDigestMaintainingObserver(pDigest));
    }
    /** Creates a new instance, which calculates a signature on the given stream,
     * using a {@link MessageDigest} with the given algorithm.
     */
    public MessageDigestCalculatingInputStream(InputStream pStream, String pAlgorithm) throws NoSuchAlgorithmException {
        this(pStream, MessageDigest.getInstance(pAlgorithm));
    }
    /** Creates a new instance, which calculates a signature on the given stream,
     * using a {@link MessageDigest} with the "MD5" algorithm.
     */
    public MessageDigestCalculatingInputStream(InputStream pStream) throws NoSuchAlgorithmException {
        this(pStream, MessageDigest.getInstance("MD5"));
    }

    /** Returns the {@link MessageDigest}, which is being used for generating the
     * checksum.
     * {@em Note}: The checksum will only reflect the data, which has been read so far.
     * This is probably not, what you expect. Make sure, that the complete data has been
     * read, if that is what you want. The easiest way to do so is by invoking
     * {@link #consume()}.
     */
    public MessageDigest getMessageDigest() {
        return messageDigest;
    }
}
