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
package org.apache.commons.io;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * This class provides static utility methods for buffered
 * copying between sources ({@link InputStream}, {@link Reader},
 * {@link String} and {@code byte[]}) and destinations
 * ({@link OutputStream}, {@link Writer}, {@link String} and
 * {@code byte[]}).
 * <p>
 * Unless otherwise noted, these {@code copy} methods do <em>not</em>
 * flush or close the streams. Often doing so would require making non-portable
 * assumptions about the streams' origin and further use. This means that both
 * streams' {@code close()} methods must be called after copying. if one
 * omits this step, then the stream resources (sockets, file descriptors) are
 * released when the associated Stream is garbage-collected. It is not a good
 * idea to rely on this mechanism. For a good overview of the distinction
 * between "memory management" and "resource management", see
 * <a href="http://www.unixreview.com/articles/1998/9804/9804ja/ja.htm">this
 * UnixReview article</a>.
 * <p>
 * For byte-to-char methods, a {@code copy} variant allows the encoding
 * to be selected (otherwise the platform default is used). We would like to
 * encourage you to always specify the encoding because relying on the platform
 * default can lead to unexpected results.
 * <p>
 * We don't provide special variants for the {@code copy} methods that
 * let you specify the buffer size because in modern VMs the impact on speed
 * seems to be minimal. We're using a default buffer size of 4 KB.
 * <p>
 * The {@code copy} methods use an internal buffer when copying. It is
 * therefore advisable <em>not</em> to deliberately wrap the stream arguments
 * to the {@code copy} methods in {@code Buffered*} streams. For
 * example, don't do the following:
 * <pre>
 *  copy( new BufferedInputStream( in ), new BufferedOutputStream( out ) );
 *  </pre>
 * The rationale is as follows:
 * <p>
 * Imagine that an InputStream's read() is a very expensive operation, which
 * would usually suggest wrapping in a BufferedInputStream. The
 * BufferedInputStream works by issuing infrequent
 * {@link InputStream#read(byte[] b, int off, int len)} requests on the
 * underlying InputStream, to fill an internal buffer, from which further
 * {@code read} requests can inexpensively get their data (until the buffer
 * runs out).
 * <p>
 * However, the {@code copy} methods do the same thing, keeping an
 * internal buffer, populated by
 * {@link InputStream#read(byte[] b, int off, int len)} requests. Having two
 * buffers (or three if the destination stream is also buffered) is pointless,
 * and the unnecessary buffer management hurts performance slightly (about 3%,
 * according to some simple experiments).
 * <p>
 * Behold, intrepid explorers; a map of this class:
 * <pre>
 *       Method      Input               Output          Dependency
 *       ------      -----               ------          -------
 * 1     copy        InputStream         OutputStream    (primitive)
 * 2     copy        Reader              Writer          (primitive)
 *
 * 3     copy        InputStream         Writer          2
 *
 * 4     copy        Reader              OutputStream    2
 *
 * 5     copy        String              OutputStream    2
 * 6     copy        String              Writer          (trivial)
 *
 * 7     copy        byte[]              Writer          3
 * 8     copy        byte[]              OutputStream    (trivial)
 * </pre>
 * <p>
 * Note that only the first two methods shuffle bytes; the rest use these
 * two, or (if possible) copy using native Java copy methods. As there are
 * method variants to specify the encoding, each row may
 * correspond to up to 2 methods.
 * <p>
 * Provenance: Excalibur.
 *
 * @deprecated Use IOUtils. Will be removed in 3.0.
 *  Methods renamed to IOUtils.write() or IOUtils.copy().
 *  Null handling behavior changed in IOUtils (null data does not
 *  throw NullPointerException).
 */
@Deprecated
public class CopyUtils {

    /**
     * Copies bytes from a {@code byte[]} to an {@link OutputStream}.
     * @param input the byte array to read from
     * @param output the {@link OutputStream} to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(final byte[] input, final OutputStream output) throws IOException {
        output.write(input);
    }

    /**
     * Copies and convert bytes from a {@code byte[]} to chars on a
     * {@link Writer}.
     * The platform's default encoding is used for the byte-to-char conversion.
     *
     * @param input the byte array to read from
     * @param output the {@link Writer} to write to
     * @throws IOException In case of an I/O problem
     * @deprecated Use {@link #copy(byte[], Writer, String)} instead
     */
    @Deprecated
    public static void copy(final byte[] input, final Writer output) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        copy(inputStream, output);
    }

    /**
     * Copies and convert bytes from a {@code byte[]} to chars on a
     * {@link Writer}, using the specified encoding.
     *
     * @param input the byte array to read from
     * @param output the {@link Writer} to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(final byte[] input, final Writer output, final String encoding) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        copy(inputStream, output, encoding);
    }

    /**
     * Copies bytes from an {@link InputStream} to an
     * {@link OutputStream}.
     *
     * @param input the {@link InputStream} to read from
     * @param output the {@link OutputStream} to write to
     * @return the number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = IOUtils.byteArray();
        int count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copies and convert bytes from an {@link InputStream} to chars on a
     * {@link Writer}.
     * <p>
     * This method uses the virtual machine's {@link Charset#defaultCharset() default charset} for byte-to-char conversion.
     * </p>
     *
     * @param input the {@link InputStream} to read from
     * @param output the {@link Writer} to write to
     * @throws IOException In case of an I/O problem
     * @deprecated Use {@link #copy(InputStream, Writer, String)} instead
     */
    @Deprecated
    public static void copy(
            final InputStream input,
            final Writer output)
                throws IOException {
        // make explicit the dependency on the default encoding
        final InputStreamReader in = new InputStreamReader(input, Charset.defaultCharset());
        copy(in, output);
    }

    /**
     * Copies and convert bytes from an {@link InputStream} to chars on a
     * {@link Writer}, using the specified encoding.
     *
     * @param input the {@link InputStream} to read from
     * @param output the {@link Writer} to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            final InputStream input,
            final Writer output,
            final String encoding)
                throws IOException {
        final InputStreamReader in = new InputStreamReader(input, encoding);
        copy(in, output);
    }

    /**
     * Serialize chars from a {@link Reader} to bytes on an
     * {@link OutputStream}, and flush the {@link OutputStream}.
     * <p>
     * This method uses the virtual machine's {@link Charset#defaultCharset() default charset} for byte-to-char conversion.
     * </p>
     *
     * @param input the {@link Reader} to read from
     * @param output the {@link OutputStream} to write to
     * @throws IOException In case of an I/O problem
     * @deprecated Use {@link #copy(Reader, OutputStream, String)} instead
     */
    @Deprecated
    public static void copy(
            final Reader input,
            final OutputStream output)
                throws IOException {
        // make explicit the dependency on the default encoding
        final OutputStreamWriter out = new OutputStreamWriter(output, Charset.defaultCharset());
        copy(input, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Serialize chars from a {@link Reader} to bytes on an
     * {@link OutputStream}, and flush the {@link OutputStream}.
     *
     * @param input the {@link Reader} to read from
     * @param output the {@link OutputStream} to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     * @since 2.5
     */
    public static void copy(
            final Reader input,
            final OutputStream output,
            final String encoding)
                throws IOException {
        final OutputStreamWriter out = new OutputStreamWriter(output, encoding);
        copy(input, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Copies chars from a {@link Reader} to a {@link Writer}.
     *
     * @param input the {@link Reader} to read from
     * @param output the {@link Writer} to write to
     * @return the number of characters copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(
            final Reader input,
            final Writer output)
                throws IOException {
        final char[] buffer = IOUtils.getScratchCharArray();
        int count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Serialize chars from a {@link String} to bytes on an
     * {@link OutputStream}, and
     * flush the {@link OutputStream}.
     * <p>
     * This method uses the virtual machine's {@link Charset#defaultCharset() default charset} for byte-to-char conversion.
     * </p>
     *
     * @param input the {@link String} to read from
     * @param output the {@link OutputStream} to write to
     * @throws IOException In case of an I/O problem
     * @deprecated Use {@link #copy(String, OutputStream, String)} instead
     */
    @Deprecated
    public static void copy(
            final String input,
            final OutputStream output)
                throws IOException {
        final StringReader in = new StringReader(input);
        // make explicit the dependency on the default encoding
        final OutputStreamWriter out = new OutputStreamWriter(output, Charset.defaultCharset());
        copy(in, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Serialize chars from a {@link String} to bytes on an
     * {@link OutputStream}, and
     * flush the {@link OutputStream}.
     *
     * @param input the {@link String} to read from
     * @param output the {@link OutputStream} to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     * @since 2.5
     */
    public static void copy(
            final String input,
            final OutputStream output,
            final String encoding)
                throws IOException {
        final StringReader in = new StringReader(input);
        final OutputStreamWriter out = new OutputStreamWriter(output, encoding);
        copy(in, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we
        // have to flush here.
        out.flush();
    }

    /**
     * Copies chars from a {@link String} to a {@link Writer}.
     *
     * @param input the {@link String} to read from
     * @param output the {@link Writer} to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(final String input, final Writer output)
                throws IOException {
        output.write(input);
    }

    /**
     * Instances should NOT be constructed in standard programming.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public CopyUtils() {
        // empty
    }

}
