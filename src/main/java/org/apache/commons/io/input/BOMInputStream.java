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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * This class is used to wrap a stream that includes an encoded {@link ByteOrderMark} as its first bytes.
 * <p>
 * This class detects these bytes and, if required, can automatically skip them and return the subsequent byte as the
 * first byte in the stream.
 * </p>
 * <p>
 * The {@link ByteOrderMark} implementation has the following predefined BOMs:
 * </p>
 * <ul>
 * <li>UTF-8 - {@link ByteOrderMark#UTF_8}</li>
 * <li>UTF-16BE - {@link ByteOrderMark#UTF_16LE}</li>
 * <li>UTF-16LE - {@link ByteOrderMark#UTF_16BE}</li>
 * <li>UTF-32BE - {@link ByteOrderMark#UTF_32LE}</li>
 * <li>UTF-32LE - {@link ByteOrderMark#UTF_32BE}</li>
 * </ul>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <h2>Example 1 - Detecting and excluding a UTF-8 BOM</h2>
 *
 * <pre>
 * BOMInputStream bomIn = BOMInputStream.builder().setInputStream(in).get();
 * if (bomIn.hasBOM()) {
 *     // has a UTF-8 BOM
 * }
 * </pre>
 *
 * <h2>Example 2 - Detecting a UTF-8 BOM without excluding it</h2>
 *
 * <pre>
 * boolean include = true;
 * BOMInputStream bomIn = BOMInputStream.builder()
 *     .setInputStream(in)
 *     .setInclude(include)
 *     .get();
 * if (bomIn.hasBOM()) {
 *     // has a UTF-8 BOM
 * }
 * </pre>
 *
 * <h2>Example 3 - Detecting Multiple BOMs</h2>
 *
 * <pre>
 * BOMInputStream bomIn = BOMInputStream.builder()
 *   .setInputStream(in)
 *   .setByteOrderMarks(ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)
 *   .get();
 * if (bomIn.hasBOM() == false) {
 *     // No BOM found
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_16LE)) {
 *     // has a UTF-16LE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_16BE)) {
 *     // has a UTF-16BE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_32LE)) {
 *     // has a UTF-32LE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_32BE)) {
 *     // has a UTF-32BE BOM
 * }
 * </pre>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @see org.apache.commons.io.ByteOrderMark
 * @see <a href="https://en.wikipedia.org/wiki/Byte_order_mark">Wikipedia - Byte Order Mark</a>
 * @since 2.0
 */
public class BOMInputStream extends ProxyInputStream {

    // @formatter:off
    /**
     * Builds a new {@link BOMInputStream}.
     *
     * <h2>Using NIO</h2>
     * <pre>{@code
     * BOMInputStream s = BOMInputStream.builder()
     *   .setPath(Paths.get("MyFile.xml"))
     *   .setByteOrderMarks(ByteOrderMark.UTF_8)
     *   .setInclude(false)
     *   .get();}
     * </pre>
     * <h2>Using IO</h2>
     * <pre>{@code
     * BOMInputStream s = BOMInputStream.builder()
     *   .setFile(new File("MyFile.xml"))
     *   .setByteOrderMarks(ByteOrderMark.UTF_8)
     *   .setInclude(false)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<BOMInputStream, Builder> {

        private static final ByteOrderMark[] DEFAULT = { ByteOrderMark.UTF_8 };

        /**
         * For test access.
         *
         * @return the default byte order mark
         */
        static ByteOrderMark getDefaultByteOrderMark() {
            return DEFAULT[0];
        }

        private ByteOrderMark[] byteOrderMarks = DEFAULT;

        private boolean include;

        /**
         * Builds a new {@link BOMInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects: InputStream, OpenOption[], include, and ByteOrderMark[].
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>include}</li>
         * <li>byteOrderMarks</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public BOMInputStream get() throws IOException {
            return new BOMInputStream(getInputStream(), include, byteOrderMarks);
        }

        /**
         * Sets the ByteOrderMarks to detect and optionally exclude.
         * <p>
         * The default is {@link ByteOrderMark#UTF_8}.
         * </p>
         *
         * @param byteOrderMarks the ByteOrderMarks to detect and optionally exclude.
         * @return {@code this} instance.
         */
        public Builder setByteOrderMarks(final ByteOrderMark... byteOrderMarks) {
            this.byteOrderMarks = byteOrderMarks != null ? byteOrderMarks.clone() : DEFAULT;
            return this;
        }

        /**
         * Sets whether to include the UTF-8 BOM (true) or to exclude it (false).
         * <p>
         * The default is false.
         * </p>
         *
         * @param include true to include the UTF-8 BOM or false to exclude it. return this;
         * @return {@code this} instance.
         */
        public Builder setInclude(final boolean include) {
            this.include = include;
            return this;
        }

    }

    /**
     * Compares ByteOrderMark objects in descending length order.
     */
    private static final Comparator<ByteOrderMark> ByteOrderMarkLengthComparator = Comparator.comparing(ByteOrderMark::length).reversed();

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
     * BOMs are sorted from longest to shortest.
     */
    private final List<ByteOrderMark> boms;

    private ByteOrderMark byteOrderMark;
    private int fbIndex;
    private int fbLength;
    private int[] firstBytes;
    private final boolean include;
    private boolean markedAtStart;
    private int markFbIndex;

    /**
     * Constructs a new BOM InputStream that excludes a {@link ByteOrderMark#UTF_8} BOM.
     *
     * @param delegate
     *            the InputStream to delegate to
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BOMInputStream(final InputStream delegate) {
        this(delegate, false, Builder.DEFAULT);
    }

    /**
     * Constructs a new BOM InputStream that detects a {@link ByteOrderMark#UTF_8} and optionally includes it.
     *
     * @param delegate
     *            the InputStream to delegate to
     * @param include
     *            true to include the UTF-8 BOM or false to exclude it
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BOMInputStream(final InputStream delegate, final boolean include) {
        this(delegate, include, Builder.DEFAULT);
    }

    /**
     * Constructs a new BOM InputStream that detects the specified BOMs and optionally includes them.
     *
     * @param delegate
     *            the InputStream to delegate to
     * @param include
     *            true to include the specified BOMs or false to exclude them
     * @param boms
     *            The BOMs to detect and optionally exclude
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BOMInputStream(final InputStream delegate, final boolean include, final ByteOrderMark... boms) {
        super(delegate);
        if (IOUtils.length(boms) == 0) {
            throw new IllegalArgumentException("No BOMs specified");
        }
        this.include = include;
        final List<ByteOrderMark> list = Arrays.asList(boms);
        // Sort the BOMs to match the longest BOM first because some BOMs have the same starting two bytes.
        list.sort(ByteOrderMarkLengthComparator);
        this.boms = list;
    }

    /**
     * Constructs a new BOM InputStream that excludes the specified BOMs.
     *
     * @param delegate
     *            the InputStream to delegate to
     * @param boms
     *            The BOMs to detect and exclude
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BOMInputStream(final InputStream delegate, final ByteOrderMark... boms) {
        this(delegate, false, boms);
    }

    /**
     * Find a BOM with the specified bytes.
     *
     * @return The matched BOM or null if none matched
     */
    private ByteOrderMark find() {
        return boms.stream().filter(this::matches).findFirst().orElse(null);
    }

    /**
     * Gets the BOM (Byte Order Mark).
     *
     * @return The BOM or null if none
     * @throws IOException
     *             if an error reading the first bytes of the stream occurs
     */
    public ByteOrderMark getBOM() throws IOException {
        if (firstBytes == null) {
            fbLength = 0;
            // BOMs are sorted from longest to shortest
            final int maxBomSize = boms.get(0).length();
            firstBytes = new int[maxBomSize];
            // Read first maxBomSize bytes
            for (int i = 0; i < firstBytes.length; i++) {
                firstBytes[i] = in.read();
                fbLength++;
                if (firstBytes[i] < 0) {
                    break;
                }
            }
            // match BOM in firstBytes
            byteOrderMark = find();
            if (byteOrderMark != null && !include) {
                if (byteOrderMark.length() < firstBytes.length) {
                    fbIndex = byteOrderMark.length();
                } else {
                    fbLength = 0;
                }
            }
        }
        return byteOrderMark;
    }

    /**
     * Gets the BOM charset Name - {@link ByteOrderMark#getCharsetName()}.
     *
     * @return The BOM charset Name or null if no BOM found
     * @throws IOException
     *             if an error reading the first bytes of the stream occurs
     */
    public String getBOMCharsetName() throws IOException {
        getBOM();
        return byteOrderMark == null ? null : byteOrderMark.getCharsetName();
    }

    /**
     * Tests whether the stream contains one of the specified BOMs.
     *
     * @return true if the stream has one of the specified BOMs, otherwise false if it does not
     * @throws IOException
     *             if an error reading the first bytes of the stream occurs
     */
    public boolean hasBOM() throws IOException {
        return getBOM() != null;
    }

    /**
     * Tests whether the stream contains the specified BOM.
     *
     * @param bom
     *            The BOM to check for
     * @return true if the stream has the specified BOM, otherwise false if it does not
     * @throws IllegalArgumentException
     *             if the BOM is not one the stream is configured to detect
     * @throws IOException
     *             if an error reading the first bytes of the stream occurs
     */
    public boolean hasBOM(final ByteOrderMark bom) throws IOException {
        if (!boms.contains(bom)) {
            throw new IllegalArgumentException("Stream not configured to detect " + bom);
        }
        return Objects.equals(getBOM(), bom);
    }

    /**
     * Invokes the delegate's {@code mark(int)} method.
     *
     * @param readLimit
     *            read ahead limit
     */
    @Override
    public synchronized void mark(final int readLimit) {
        markFbIndex = fbIndex;
        markedAtStart = firstBytes == null;
        in.mark(readLimit);
    }

    /**
     * Checks if the bytes match a BOM.
     *
     * @param bom
     *            The BOM
     * @return true if the bytes match the bom, otherwise false
     */
    private boolean matches(final ByteOrderMark bom) {
        // if (bom.length() != fbLength) {
        // return false;
        // }
        // firstBytes may be bigger than the BOM bytes
        for (int i = 0; i < bom.length(); i++) {
            if (bom.get(i) != firstBytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Invokes the delegate's {@code read()} method, detecting and optionally skipping BOM.
     *
     * @return the byte read (excluding BOM) or -1 if the end of stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        checkOpen();
        final int b = readFirstBytes();
        return b >= 0 ? b : in.read();
    }

    /**
     * Invokes the delegate's {@code read(byte[])} method, detecting and optionally skipping BOM.
     *
     * @param buf
     *            the buffer to read the bytes into
     * @return the number of bytes read (excluding BOM) or -1 if the end of stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public int read(final byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Invokes the delegate's {@code read(byte[], int, int)} method, detecting and optionally skipping BOM.
     *
     * @param buf
     *            the buffer to read the bytes into
     * @param off
     *            The start offset
     * @param len
     *            The number of bytes to read (excluding BOM)
     * @return the number of bytes read or -1 if the end of stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public int read(final byte[] buf, int off, int len) throws IOException {
        int firstCount = 0;
        int b = 0;
        while (len > 0 && b >= 0) {
            b = readFirstBytes();
            if (b >= 0) {
                buf[off++] = (byte) (b & 0xFF);
                len--;
                firstCount++;
            }
        }
        final int secondCount = in.read(buf, off, len);
        return secondCount < 0 ? firstCount > 0 ? firstCount : EOF : firstCount + secondCount;
    }

    /**
     * This method reads and either preserves or skips the first bytes in the stream. It behaves like the single-byte
     * {@code read()} method, either returning a valid byte or -1 to indicate that the initial bytes have been
     * processed already.
     *
     * @return the byte read (excluding BOM) or -1 if the end of stream
     * @throws IOException
     *             if an I/O error occurs
     */
    private int readFirstBytes() throws IOException {
        getBOM();
        return fbIndex < fbLength ? firstBytes[fbIndex++] : EOF;
    }

    /**
     * Invokes the delegate's {@code reset()} method.
     *
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public synchronized void reset() throws IOException {
        fbIndex = markFbIndex;
        if (markedAtStart) {
            firstBytes = null;
        }

        in.reset();
    }

    /**
     * Invokes the delegate's {@code skip(long)} method, detecting and optionally skipping BOM.
     *
     * @param n
     *            the number of bytes to skip
     * @return the number of bytes to skipped or -1 if the end of stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public long skip(final long n) throws IOException {
        int skipped = 0;
        while (n > skipped && readFirstBytes() >= 0) {
            skipped++;
        }
        return in.skip(n - skipped) + skipped;
    }
}
