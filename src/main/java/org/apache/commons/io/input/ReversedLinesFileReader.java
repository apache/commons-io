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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileSystem;
import org.apache.commons.io.StandardLineSeparator;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Reads lines in a file reversely (similar to a BufferedReader, but starting at the last line). Useful for e.g. searching in log files.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @since 2.2
 */
public class ReversedLinesFileReader implements Closeable {

    // @formatter:off
    /**
     * Builds a new {@link ReversedLinesFileReader}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * ReversedLinesFileReader r = ReversedLinesFileReader.builder()
     *   .setPath(path)
     *   .setBufferSize(4096)
     *   .setCharset(StandardCharsets.UTF_8)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<ReversedLinesFileReader, Builder> {

        /**
         * Constructs a new {@link Builder}.
         */
        public Builder() {
            setBufferSizeDefault(DEFAULT_BLOCK_SIZE);
            setBufferSize(DEFAULT_BLOCK_SIZE);
        }

        /**
         * Builds a new {@link ReversedLinesFileReader}.
         * <p>
         * You must set input that supports {@link #getInputStream()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link #getBufferSize()}</li>
         * <li>{@link #getCharset()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to a {@link Path}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getPath()
         * @see #getBufferSize()
         * @see #getCharset()
         */
        @Override
        public ReversedLinesFileReader get() throws IOException {
            return new ReversedLinesFileReader(getPath(), getBufferSize(), getCharset());
        }

    }

    private final class FilePart {
        private final long no;

        private final byte[] data;

        private byte[] leftOver;

        private int currentLastBytePos;

        /**
         * Constructs a new instance.
         *
         * @param no                     the part number
         * @param length                 its length
         * @param leftOverOfLastFilePart remainder
         * @throws IOException if there is a problem reading the file
         */
        private FilePart(final long no, final int length, final byte[] leftOverOfLastFilePart) throws IOException {
            this.no = no;
            final int dataLength = length + (leftOverOfLastFilePart != null ? leftOverOfLastFilePart.length : 0);
            this.data = new byte[dataLength];
            final long off = (no - 1) * blockSize;

            // read data
            if (no > 0 /* file not empty */) {
                channel.position(off);
                final int countRead = channel.read(ByteBuffer.wrap(data, 0, length));
                if (countRead != length) {
                    throw new IllegalStateException("Count of requested bytes and actually read bytes don't match");
                }
            }
            // copy left over part into data arr
            if (leftOverOfLastFilePart != null) {
                System.arraycopy(leftOverOfLastFilePart, 0, data, length, leftOverOfLastFilePart.length);
            }
            this.currentLastBytePos = data.length - 1;
            this.leftOver = null;
        }

        /**
         * Constructs the buffer containing any leftover bytes.
         */
        private void createLeftOver() {
            final int lineLengthBytes = currentLastBytePos + 1;
            if (lineLengthBytes > 0) {
                // create left over for next block
                leftOver = Arrays.copyOf(data, lineLengthBytes);
            } else {
                leftOver = null;
            }
            currentLastBytePos = -1;
        }

        /**
         * Finds the new-line sequence and return its length.
         *
         * @param data buffer to scan
         * @param i    start offset in buffer
         * @return length of newline sequence or 0 if none found
         */
        private int getNewLineMatchByteCount(final byte[] data, final int i) {
            for (final byte[] newLineSequence : newLineSequences) {
                boolean match = true;
                for (int j = newLineSequence.length - 1; j >= 0; j--) {
                    final int k = i + j - (newLineSequence.length - 1);
                    match &= k >= 0 && data[k] == newLineSequence[j];
                }
                if (match) {
                    return newLineSequence.length;
                }
            }
            return 0;
        }

        /**
         * Reads a line.
         *
         * @return the line or null
         */
        private String readLine() { //NOPMD Bug in PMD

            String line = null;
            int newLineMatchByteCount;

            final boolean isLastFilePart = no == 1;

            int i = currentLastBytePos;
            while (i > -1) {

                if (!isLastFilePart && i < avoidNewlineSplitBufferSize) {
                    // avoidNewlineSplitBuffer: for all except the last file part we
                    // take a few bytes to the next file part to avoid splitting of newlines
                    createLeftOver();
                    break; // skip last few bytes and leave it to the next file part
                }

                // check for newline
                if ((newLineMatchByteCount = getNewLineMatchByteCount(data, i)) > 0 /* found newline */) {
                    final int lineStart = i + 1;
                    final int lineLengthBytes = currentLastBytePos - lineStart + 1;

                    if (lineLengthBytes < 0) {
                        throw new IllegalStateException("Unexpected negative line length=" + lineLengthBytes);
                    }
                    final byte[] lineData = Arrays.copyOfRange(data, lineStart, lineStart + lineLengthBytes);

                    line = new String(lineData, charset);

                    currentLastBytePos = i - newLineMatchByteCount;
                    break; // found line
                }

                // move cursor
                i -= byteDecrement;

                // end of file part handling
                if (i < 0) {
                    createLeftOver();
                    break; // end of file part
                }
            }

            // last file part handling
            if (isLastFilePart && leftOver != null) {
                // there will be no line break anymore, this is the first line of the file
                line = new String(leftOver, charset);
                leftOver = null;
            }

            return line;
        }

        /**
         * Handles block rollover
         *
         * @return the new FilePart or null
         * @throws IOException if there was a problem reading the file
         */
        private FilePart rollOver() throws IOException {

            if (currentLastBytePos > -1) {
                throw new IllegalStateException("Current currentLastCharPos unexpectedly positive... "
                        + "last readLine() should have returned something! currentLastCharPos=" + currentLastBytePos);
            }

            if (no > 1) {
                return new FilePart(no - 1, blockSize, leftOver);
            }
            // NO 1 was the last FilePart, we're finished
            if (leftOver != null) {
                throw new IllegalStateException("Unexpected leftover of the last block: leftOverOfThisFilePart="
                        + new String(leftOver, charset));
            }
            return null;
        }
    }

    private static final String EMPTY_STRING = "";

    private static final int DEFAULT_BLOCK_SIZE = FileSystem.getCurrent().getBlockSize();

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final int blockSize;
    private final Charset charset;
    private final SeekableByteChannel channel;
    private final long totalByteLength;
    private final long totalBlockCount;
    private final byte[][] newLineSequences;
    private final int avoidNewlineSplitBufferSize;
    private final int byteDecrement;
    private FilePart currentFilePart;
    private boolean trailingNewlineOfFileSkipped;

    /**
     * Constructs a ReversedLinesFileReader with default block size of 4KB and the
     * platform's default encoding.
     *
     * @param file the file to be read
     * @throws IOException if an I/O error occurs.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final File file) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE, Charset.defaultCharset());
    }

    /**
     * Constructs a ReversedLinesFileReader with default block size of 4KB and the
     * specified encoding.
     *
     * @param file    the file to be read
     * @param charset the charset to use, null uses the default Charset.
     * @throws IOException if an I/O error occurs.
     * @since 2.5
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final File file, final Charset charset) throws IOException {
        this(file.toPath(), charset);
    }

    /**
     * Constructs a ReversedLinesFileReader with the given block size and encoding.
     *
     * @param file      the file to be read
     * @param blockSize size of the internal buffer (for ideal performance this
     *                  should match with the block size of the underlying file
     *                  system).
     * @param charset  the encoding of the file, null uses the default Charset.
     * @throws IOException if an I/O error occurs.
     * @since 2.3
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final File file, final int blockSize, final Charset charset) throws IOException {
        this(file.toPath(), blockSize, charset);
    }

    /**
     * Constructs a ReversedLinesFileReader with the given block size and encoding.
     *
     * @param file      the file to be read
     * @param blockSize size of the internal buffer (for ideal performance this
     *                  should match with the block size of the underlying file
     *                  system).
     * @param charsetName  the encoding of the file, null uses the default Charset.
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException if the encoding is not supported
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final File file, final int blockSize, final String charsetName) throws IOException {
        this(file.toPath(), blockSize, charsetName);
    }

    /**
     * Constructs a ReversedLinesFileReader with default block size of 4KB and the
     * specified encoding.
     *
     * @param file    the file to be read
     * @param charset the charset to use, null uses the default Charset.
     * @throws IOException if an I/O error occurs.
     * @since 2.7
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final Path file, final Charset charset) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE, charset);
    }

    /**
     * Constructs a ReversedLinesFileReader with the given block size and encoding.
     *
     * @param file      the file to be read
     * @param blockSize size of the internal buffer (for ideal performance this
     *                  should match with the block size of the underlying file
     *                  system).
     * @param charset  the encoding of the file, null uses the default Charset.
     * @throws IOException if an I/O error occurs.
     * @since 2.7
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final Path file, final int blockSize, final Charset charset) throws IOException {
        this.blockSize = blockSize;
        this.charset = Charsets.toCharset(charset);

        // --- check & prepare encoding ---
        final CharsetEncoder charsetEncoder = this.charset.newEncoder();
        final float maxBytesPerChar = charsetEncoder.maxBytesPerChar();
        if (maxBytesPerChar == 1f || this.charset == StandardCharsets.UTF_8) {
            // all one byte encodings are no problem
            byteDecrement = 1;
        } else if (this.charset == Charset.forName("Shift_JIS") || // Same as for UTF-8
        // http://www.herongyang.com/Unicode/JIS-Shift-JIS-Encoding.html
                this.charset == Charset.forName("windows-31j") || // Windows code page 932 (Japanese)
                this.charset == Charset.forName("x-windows-949") || // Windows code page 949 (Korean)
                this.charset == Charset.forName("gbk") || // Windows code page 936 (Simplified Chinese)
                this.charset == Charset.forName("x-windows-950")) { // Windows code page 950 (Traditional Chinese)
            byteDecrement = 1;
        } else if (this.charset == StandardCharsets.UTF_16BE || this.charset == StandardCharsets.UTF_16LE) {
            // UTF-16 new line sequences are not allowed as second tuple of four byte
            // sequences,
            // however byte order has to be specified
            byteDecrement = 2;
        } else if (this.charset == StandardCharsets.UTF_16) {
            throw new UnsupportedEncodingException(
                    "For UTF-16, you need to specify the byte order (use UTF-16BE or " + "UTF-16LE)");
        } else {
            throw new UnsupportedEncodingException(
                    "Encoding " + charset + " is not supported yet (feel free to " + "submit a patch)");
        }

        // NOTE: The new line sequences are matched in the order given, so it is
        // important that \r\n is BEFORE \n
        this.newLineSequences = new byte[][] {
            StandardLineSeparator.CRLF.getBytes(this.charset),
            StandardLineSeparator.LF.getBytes(this.charset),
            StandardLineSeparator.CR.getBytes(this.charset)
        };

        this.avoidNewlineSplitBufferSize = newLineSequences[0].length;

        // Open file
        this.channel = Files.newByteChannel(file, StandardOpenOption.READ);
        this.totalByteLength = channel.size();
        int lastBlockLength = (int) (this.totalByteLength % blockSize);
        if (lastBlockLength > 0) {
            this.totalBlockCount = this.totalByteLength / blockSize + 1;
        } else {
            this.totalBlockCount = this.totalByteLength / blockSize;
            if (this.totalByteLength > 0) {
                lastBlockLength = blockSize;
            }
        }
        this.currentFilePart = new FilePart(totalBlockCount, lastBlockLength, null);

    }

    /**
     * Constructs a ReversedLinesFileReader with the given block size and encoding.
     *
     * @param file        the file to be read
     * @param blockSize   size of the internal buffer (for ideal performance this
     *                    should match with the block size of the underlying file
     *                    system).
     * @param charsetName the encoding of the file, null uses the default Charset.
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException if the encoding is not supported
     * @since 2.7
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReversedLinesFileReader(final Path file, final int blockSize, final String charsetName) throws IOException {
        this(file, blockSize, Charsets.toCharset(charsetName));
    }

    /**
     * Closes underlying resources.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Returns the lines of the file from bottom to top.
     *
     * @return the next line or null if the start of the file is reached
     * @throws IOException if an I/O error occurs.
     */
    public String readLine() throws IOException {

        String line = currentFilePart.readLine();
        while (line == null) {
            currentFilePart = currentFilePart.rollOver();
            if (currentFilePart == null) {
                // no more FileParts: we're done, leave line set to null
                break;
            }
            line = currentFilePart.readLine();
        }

        // aligned behavior with BufferedReader that doesn't return a last, empty line
        if (EMPTY_STRING.equals(line) && !trailingNewlineOfFileSkipped) {
            trailingNewlineOfFileSkipped = true;
            line = readLine();
        }

        return line;
    }

    /**
     * Returns {@code lineCount} lines of the file from bottom to top.
     * <p>
     * If there are less than {@code lineCount} lines in the file, then that's what
     * you get.
     * </p>
     * <p>
     * Note: You can easily flip the result with {@link Collections#reverse(List)}.
     * </p>
     *
     * @param lineCount How many lines to read.
     * @return A new list
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public List<String> readLines(final int lineCount) throws IOException {
        if (lineCount < 0) {
            throw new IllegalArgumentException("lineCount < 0");
        }
        final ArrayList<String> arrayList = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            final String line = readLine();
            if (line == null) {
                return arrayList;
            }
            arrayList.add(line);
        }
        return arrayList;
    }

    /**
     * Returns the last {@code lineCount} lines of the file.
     * <p>
     * If there are less than {@code lineCount} lines in the file, then that's what
     * you get.
     * </p>
     *
     * @param lineCount How many lines to read.
     * @return A String.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public String toString(final int lineCount) throws IOException {
        final List<String> lines = readLines(lineCount);
        Collections.reverse(lines);
        return lines.isEmpty() ? EMPTY_STRING : String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

}
