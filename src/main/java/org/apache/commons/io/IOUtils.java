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
package org.apache.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.AppendableWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.ThresholdingOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

/**
 * General IO stream manipulation utilities.
 * <p>
 * This class provides static utility methods for input/output operations.
 * </p>
 * <ul>
 * <li>closeQuietly - these methods close a stream ignoring nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p>
 * The byte-to-char methods and char-to-byte methods involve a conversion step.
 * Two methods are provided in each case, one that uses the platform default
 * encoding and the other which allows you to specify an encoding. You are
 * encouraged to always specify an encoding, because relying on the platform
 * default can lead to unexpected results - for example, when moving from
 * development to production.
 * </p>
 * <p>
 * All the methods in this class that read a stream are buffered internally.
 * This means that there is no cause to use a {@link BufferedInputStream}
 * or {@link BufferedReader}. The default buffer size of 8K has been shown
 * to be efficient in tests.
 * </p>
 * <p>
 * The various copy methods all delegate the actual copying to one of the following methods:
 * </p>
 * <ul>
 * <li>{@link #copyLarge(InputStream, OutputStream, byte[])}</li>
 * <li>{@link #copyLarge(InputStream, OutputStream, long, long, byte[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, char[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, long, long, char[])}</li>
 * </ul>
 * For example, {@link #copy(InputStream, OutputStream)} calls {@link #copyLarge(InputStream, OutputStream)}
 * which calls {@link #copy(InputStream, OutputStream, int)} which creates the buffer and calls
 * {@link #copyLarge(InputStream, OutputStream, byte[])}.
 * <p>
 * Applications can re-use buffers by using the underlying methods directly.
 * This may improve performance for applications that need to do a lot of copying.
 * </p>
 * <p>
 * Wherever possible, the methods in this class do <em>not</em> {@link Flushable#flush()}
 * or {@link Closeable#close()} the stream. This is to avoid making non-portable
 * assumptions about the streams' origin and further use. Thus the <i>caller</i> is still
 * responsible for closing streams after use, unless otherwise specified by the method.
 * </p>
 * <p>
 * Origin of code: Excalibur.
 * </p>
 */
public class IOUtils {
    // NOTE: This class is focused on InputStream, OutputStream, Reader and
    // Writer. Each method should take at least one of these as a parameter,
    // or return one of them.

    /**
     * CR char.
     *
     * @since 2.9.0
     */
    public static final int CR = '\r';

    /**
     * The default buffer size ({@value}) to use in copy methods.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The system directory separator character.
     */
    public static final char DIR_SEPARATOR = File.separatorChar;

    /**
     * The Unix directory separator character.
     */
    public static final char DIR_SEPARATOR_UNIX = '/';

    /**
     * The Windows directory separator character.
     */
    public static final char DIR_SEPARATOR_WINDOWS = '\\';

    /**
     * A singleton empty byte array.
     *
     *  @since 2.9.0
     */
    public static final byte[] EMPTY_BYTE_ARRAY = {};

    /**
     * Represents the end-of-file (or stream).
     * @since 2.5 (made public)
     */
    public static final int EOF = -1;

    /**
     * LF char.
     *
     * @since 2.9.0
     */
    public static final int LF = '\n';

    /**
     * The system line separator string.
     *
     * @deprecated Use {@link System#lineSeparator()}.
     */
    @Deprecated
    public static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * The Unix line separator string.
     *
     * @see StandardLineSeparator#LF
     */
    public static final String LINE_SEPARATOR_UNIX = StandardLineSeparator.LF.getString();

    /**
     * The Windows line separator string.
     *
     * @see StandardLineSeparator#CRLF
     */
    public static final String LINE_SEPARATOR_WINDOWS = StandardLineSeparator.CRLF.getString();

    /**
     * Internal byte array buffer.
     */
    private static final ThreadLocal<byte[]> SKIP_BYTE_BUFFER = ThreadLocal.withInitial(IOUtils::byteArray);

    /**
     * Internal byte array buffer.
     */
    private static final ThreadLocal<char[]> SKIP_CHAR_BUFFER = ThreadLocal.withInitial(IOUtils::charArray);

    /**
     * Returns the given {@link InputStream} if it is already a {@link BufferedInputStream}, otherwise creates a
     * {@code BufferedInputStream} from the given {@code InputStream}.
     *
     * @param inputStream the {@code InputStream} to wrap or return (not {@code null}).
     * @return the given {@code InputStream} if it is already an instance of {@code BufferedInputStream}, or
     * a new {@code BufferedInputStream} for the given {@code InputStream}.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedInputStream buffer(final InputStream inputStream) {
        // reject null early on rather than waiting for IO operation to fail.
        // not checked by BufferedInputStream
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }

    /**
     * Returns the given {@link InputStream} if it is already a {@link BufferedInputStream}, otherwise creates a
     * {@code BufferedInputStream} from the given {@code InputStream}, using the specified buffer size.
     *
     * @param inputStream the {@code InputStream} to wrap or return (not {@code null}).
     * @param size the buffer size, if a new {@code BufferedInputStream} is created.
     * @return the given {@code InputStream} if it is already an instance of {@code BufferedInputStream}, or
     * a new {@code BufferedInputStream} for the given {@code InputStream}.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedInputStream buffer(final InputStream inputStream, final int size) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, size);
    }

    /**
     * Returns the given {@link OutputStream} if it is already a {@link BufferedOutputStream}, otherwise creates a
     * {@code BufferedOutputStream} from the given {@code OutputStream}.
     *
     * @param outputStream the {@code OutputStream} to wrap or return (not {@code null}).
     * @return the given {@code OutputStream} if it is already an instance of {@code BufferedOutputStream}, or
     * a new {code BufferedOutputStream} for the given {@code OutputStream}.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedOutputStream buffer(final OutputStream outputStream) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }

    /**
     * Returns the given {@link OutputStream} if it is already a {@link BufferedOutputStream}, otherwise creates a
     * {@code BufferedOutputStream} from the given {@code OutputStream}, using the specified buffer size.
     *
     * @param outputStream the {@code OutputStream} to wrap or return (not {@code null}).
     * @param size the buffer size, if a new {@code BufferedOutputStream} is created.
     * @return the given {@code OutputStream} if it is already an instance of {@code BufferedOutputStream}, or
     * a new {code BufferedOutputStream} for the given {@code OutputStream}.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedOutputStream buffer(final OutputStream outputStream, final int size) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream, size);
    }

    /**
     * Returns the given {@link Reader} if it is already a {@link BufferedReader}, otherwise creates a
     * {@code BufferedReader} from the given {@code Reader}.
     *
     * @param reader the {@code Reader} to wrap or return (not {@code null}).
     * @return the given {@code Reader} if it is already an instance of {@code BufferedReader}, or
     * a new {@code BufferedReader} for the given {@code Reader}.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @since 2.5
     */
    public static BufferedReader buffer(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Returns the given {@link Reader} if it is already a {@link BufferedReader}, otherwise creates a {@code BufferedReader}
     * from the given {@code Reader}, using the specified buffer size.
     *
     * @param reader the {@code Reader} to wrap or return (not {@code null}).
     * @param size the buffer size, if a new {@code BufferedReader} is created.
     * @return the given {@code Reader} if it is already an instance of {@code BufferedReader}, or
     * a new {@code BufferedReader} for the given {@code Reader}.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @since 2.5
     */
    public static BufferedReader buffer(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    /**
     * Returns the given {@link Writer} if it is already a {@link BufferedWriter}, otherwise creates a {@code BufferedWriter}
     * from the given {@code Writer}.
     *
     * @param writer the {@code Writer} to wrap or return (not {@code null}).
     * @return the given {@code Writer} if it is already an instance of {@code BufferedWriter}, or
     * a new {@code BufferedWriter} for the given {@code Writer}.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @since 2.5
     */
    public static BufferedWriter buffer(final Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    /**
     * Returns the given {@link Writer} if it is already a {@link BufferedWriter}, otherwise creates a {@code BufferedWriter}
     * from the given {@code Writer}, using the specified buffer size.
     *
     * @param writer the {@code Writer} to wrap or return (not {@code null}).
     * @param size the buffer size, if a new {@code BufferedWriter} is created.
     * @return the given {@code Writer} if it is already an instance of {@code BufferedWriter}, or
     * a new {@code BufferedWriter} for the given {@code Writer}.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @since 2.5
     */
    public static BufferedWriter buffer(final Writer writer, final int size) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
    }

    /**
     * Returns a new {@code byte[]} array of size {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a new {@code byte[]} array of size {@link #DEFAULT_BUFFER_SIZE}.
     * @since 2.9.0
     */
    public static byte[] byteArray() {
        return byteArray(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a new {@code byte[]} array of the given size.
     *
     * TODO Consider guarding or warning against large allocations...
     *
     * @param size array size.
     * @return a new {@code byte[]} array of the given size.
     * @since 2.9.0
     */
    public static byte[] byteArray(final int size) {
        return new byte[size];
    }

    /**
     * Returns a new {@code char[]} array of size {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a new {@code char[]} array of size {@link #DEFAULT_BUFFER_SIZE}.
     * @since 2.9.0
     */
    private static char[] charArray() {
        return charArray(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a new {@code char[]} array of the given size.
     *
     * TODO Consider guarding or warning against large allocations...
     *
     * @param size array size.
     * @return a new {@code char[]} array of the given size.
     * @since 2.9.0
     */
    private static char[] charArray(final int size) {
        return new char[size];
    }

    /**
     * Closes the given {@link Closeable} as a {@code null}-safe operation.
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     * @param closeable The {@code Closeable} to close, may be {@code null} or already closed.
     * @throws IOException if an I/O error occurs.
     * @since 2.7
     */
    public static void close(final Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    /**
     * Closes the given {@link Closeable[]} as a {@code null}-safe operation.
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     * @param closeables The {@code Closeable[]} to close, may be {@code null} or already closed.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static void close(final Closeable... closeables) throws IOException {
        IOConsumer.forEach(closeables, IOUtils::close);
    }

    /**
     * Closes the given {@link Closeable} as a {@code null}-safe operation.
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     * @param closeable The {@code Closeable} to close, may be {@code null} or already closed.
     * @param consumer Consume the {@link IOException} thrown by {@link Closeable#close()}.
     * @throws IOException if an I/O error occurs.
     * @since 2.7
     */
    public static void close(final Closeable closeable, final IOConsumer<IOException> consumer) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

    /**
     * Closes a URLConnection.
     *
     * @param conn the connection to close.
     * @since 2.4
     */
    public static void close(final URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    /**
     * Closes a {@link Closeable} unconditionally.
     *
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * <p>
     * This is typically used in {@code finally} blocks to ensure that the {code Closeable} is closed
     * even if an {@code Exception} was thrown before the normal close statement was reached.
     * <p>
     * Example code:
     * </p>
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // process closeable
     *     closeable.close();
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(closeable);
     * }
     * </pre>
     * <p>
     * Closing all streams:
     * </p>
     * <pre>
     * try {
     *     return IOUtils.copy(inputStream, outputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     *     IOUtils.closeQuietly(outputStream);
     * }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param closeable the {@code Closeable} to close, may be {@code null} or already closed.
     * @since 2.0
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Closeable closeable) {
        closeQuietly(closeable, null);
    }

    /**
     * Closes a {@link Closeable[]} unconditionally.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * <p>
     * This is typically used in {@code finally} blocks to ensure that the {code Closeable} is closed
     * even if an {@code Exception} was thrown before the normal close statement was reached.
     * <br>
     * <b>It should not be used to replace the close statement(s)
     * which should be present for the non-exceptional case.</b>
     * <br>
     * It is only intended to simplify tidying up where normal processing has already failed
     * and reporting close failure as well is not necessary or useful.
     * <p>
     * Example code:
     * </p>
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // processing using the closeable; may throw an Exception
     *     closeable.close(); // Normal close - exceptions not ignored
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     <b>IOUtils.closeQuietly(closeable); // In case normal close was skipped due to Exception</b>
     * }
     * </pre>
     * <p>
     * Closing all streams:
     * <br>
     * <pre>
     * try {
     *     return IOUtils.copy(inputStream, outputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream, outputStream);
     * }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     * @param closeables the {@code Closeable[]} to close, may be {@code null} or already closed.
     * @see #closeQuietly(Closeable)
     * @since 2.5
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Closeable... closeables) {
        if (closeables != null) {
            Arrays.stream(closeables).forEach(IOUtils::closeQuietly);
        }
    }

    /**
     * Closes the given {@link Closeable} as a {@code null}-safe operation
     * while consuming {@link IOException} by the given {@link Consumer}.
     * <br>
     * @param closeable The {@code Closeable} to close, may be {@code null} or already closed.
     * @param consumer Consumes the {@code IOException} thrown by {@link Closeable#close()}.
     * @since 2.7
     */
    public static void closeQuietly(final Closeable closeable, final Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

    /**
     * Closes an {@link InputStream} unconditionally.
     * <p>
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   byte[] data = new byte[1024];
     *   InputStream in = null;
     *   try {
     *       in = new FileInputStream("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param input the {@code InputStream} to close, may be {@code null} or already closed.
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final InputStream input) {
        closeQuietly((Closeable) input);
    }

    /**
     * Closes an {@link OutputStream} unconditionally.
     * <p>
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     * byte[] data = "Hello, World".getBytes();
     *
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream("foo.txt");
     *     out.write(data);
     *     out.close(); //close errors are handled
     * } catch (IOException e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(out);
     * }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources statement} where appropriate.
     * </p>
     *
     * @param output the {@code OutputStream} to close, may be {@code null} or already closed.
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final OutputStream output) {
        closeQuietly((Closeable) output);
    }

    /**
     * Closes an {@link Reader} unconditionally.
     * <p>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   char[] data = new char[1024];
     *   Reader in = null;
     *   try {
     *       in = new FileReader("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param reader the {@code Reader} to close, may be {@code null} or already closed.
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Reader reader) {
        closeQuietly((Closeable) reader);
    }

    /**
     * Closes a {@link Selector} unconditionally.
     * <p>
     * Equivalent to {@link Selector#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   Selector selector = null;
     *   try {
     *       selector = Selector.open();
     *       // process socket
     *
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(selector);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param selector the {@code Selector} to close, may be {@code null} or already closed.
     * @since 2.2
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Selector selector) {
        closeQuietly((Closeable) selector);
    }

    /**
     * Closes a {@link ServerSocket} unconditionally.
     * <p>
     * Equivalent to {@link ServerSocket#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   ServerSocket socket = null;
     *   try {
     *       socket = new ServerSocket();
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param serverSocket the {@code ServerSocket} to close, may be {@code null} or already closed.
     * @since 2.2
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final ServerSocket serverSocket) {
        closeQuietly((Closeable) serverSocket);
    }

    /**
     * Closes a {@link Socket} unconditionally.
     * <p>
     * Equivalent to {@link Socket#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   Socket socket = null;
     *   try {
     *       socket = new Socket("http://www.foo.com/", 80);
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param socket the {@code Socket} to close, may be {@code null} or already closed.
     * @since 2.0
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Socket socket) {
        closeQuietly((Closeable) socket);
    }

    /**
     * Closes a {@link Writer} unconditionally.
     * <p>
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     * This is typically used in {@code finally} blocks.
     * </p>
     * <p>
     * Example code:
     * </p>
     * <pre>
     *   Writer out = null;
     *   try {
     *       out = new StringWriter();
     *       out.write("Hello World");
     *       out.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(out);
     *   }
     * </pre>
     * <p>
     * Also consider using a {@code try-with-resources} statement where appropriate.
     * </p>
     *
     * @param writer the {@code Writer} to close, may be {@code null} or already closed.
     * @see Throwable#addSuppressed(java.lang.Throwable)
     */
    public static void closeQuietly(final Writer writer) {
        closeQuietly((Closeable) writer);
    }

    /**
     * Consumes bytes from a {@link InputStream} and ignores them.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param input the {@code InputStream} to read.
     * @return the number of bytes copied. or {@code 0} if input is {@code null}.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static long consume(final InputStream input)
            throws IOException {
        return copyLarge(input, NullOutputStream.INSTANCE, getByteArray());
    }

    /**
     * Compares the contents of two {@link InputStream} instances
     * to determine if they are equal or not.
     * <p>
     * This method buffers the input internally using
     * {@link BufferedInputStream} if they are not already buffered.
     * </p>
     *
     * @param input1 the first {@code InputStream} instance.
     * @param input2 the second {@code InputStream} instance.
     * @return {@code true} if the content of the streams are equal or they both don't
     * exist, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean contentEquals(final InputStream input1, final InputStream input2) throws IOException {
        // Before making any changes, please test with
        // org.apache.commons.io.jmh.IOUtilsContentEqualsInputStreamsBenchmark
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        // reuse one
        final byte[] array1 = getByteArray();
        // allocate another
        final byte[] array2 = byteArray();
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }

    /**
     * Compares the contents of two {@link Reader} instances
     * to determine if they are equal or not.
     * <p>
     * This method buffers the input internally using
     * {@link BufferedReader} if they are not already buffered.
     * </p>
     *
     * @param input1 the first {@code Reader} instance.
     * @param input2 the second {@code Reader} instance.
     * @return {@code true} if the content of the readers are equal or they both don't
     * exist, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     * @since 1.1
     */
    public static boolean contentEquals(final Reader input1, final Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        // reuse one
        final char[] array1 = getCharArray();
        // but allocate another
        final char[] array2 = charArray();
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }

    /**
     * Compares the contents of two {@link Reader} instances
     * to determine if they are equal or not, ignoring EOL characters.
     * <p>
     * This method buffers the input internally using
     * {@link BufferedReader} if they are not already buffered.
     * </p>
     *
     * @param reader1 the first {@code Reader} instance.
     * @param reader2 the second {@code Reader} instance.
     * @return {@code true} if the content of the readers are equal (ignoring EOL differences)
     * or they both don't exist, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     * @since 2.2
     */
    @SuppressWarnings("resource")
    public static boolean contentEqualsIgnoreEOL(final Reader reader1, final Reader reader2)
            throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }
        final BufferedReader br1 = (reader1 instanceof BufferedReader ? (BufferedReader)reader1 : toBufferedReader(reader1));
        final BufferedReader br2 = (reader2 instanceof BufferedReader ? (BufferedReader)reader2 : toBufferedReader(reader2));

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        if (line1 == null && line2 == null) {
            return true;
        } else if (line1 == null ^ line2 == null) {
            return false;
        } else if (line1.isEmpty() && line2.isEmpty()) {
            return true;
        } else {
            while (line1 != null && line1.equals(line2)) {
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
        }
        return Objects.equals(line1, line2);
    }

    /**
     * Copies bytes from an {@link InputStream} to an {@link OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     * </p>
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of {@code -1} after the copy has completed since
     * the correct number of bytes cannot be returned as an {@code int}. For large streams use the
     * {@link #copyLarge(InputStream, OutputStream)} method.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null} or closed.
     * @param outputStream the {@code OutputStream} to write to, not {@code null} or closed.
     * @return the number of bytes copied, or -1 if greater than {@link Integer#MAX_VALUE}.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 1.1
     */
    public static int copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final long count = copyLarge(inputStream, outputStream);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

    /**
     * Copies bytes from an {@link InputStream} to an {@link OutputStream} using an internal buffer of the
     * given size.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null} or closed.
     * @param outputStream the {@code OutputStream} to write to, not {@code null} or closed.
     * @param bufferSize the buffer size used to copy from the input to the output.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.5
     */
    public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize)
            throws IOException {
        return copyLarge(inputStream, outputStream, IOUtils.byteArray(bufferSize));
    }

    /**
     * Copies bytes from an {@link InputStream} to chars on a
     * {@link Writer} using {@link Charset#defaultCharset()} for encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * This method uses {@link InputStreamReader}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #copy(InputStream, Writer, Charset)} instead.
     */
    @Deprecated
    public static void copy(final InputStream input, final Writer writer)
            throws IOException {
        copy(input, writer, Charset.defaultCharset());
    }

    /**
     * Copies bytes from an {@link InputStream} to chars on a
     * {@link Writer} using the specified {@link Charset} for encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * This method uses {@link InputStreamReader}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @param inputCharset the {@code Charset} to use for the {@code InputStream},
     * {@code null} uses {@link Charset#defaultCharset()}.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.3
     */
    public static void copy(final InputStream input, final Writer writer, final Charset inputCharset)
            throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(inputCharset));
        copy(reader, writer);
    }

    /**
     * Copies bytes from an {@link InputStream} to chars on a
     * {@link Writer} using the name of the requested {@link Charset} for encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link InputStreamReader}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @param inputCharsetName the name of the requested {@code Charset} for the {@code InputStream},
     * {@code null} uses {@link Charset#defaultCharset()}.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void copy(final InputStream input, final Writer writer, final String inputCharsetName)
            throws IOException {
        copy(input, writer, Charsets.toCharset(inputCharsetName));
    }

    /**
     * Copies bytes from a {@link java.io.ByteArrayOutputStream} to a {@link QueueInputStream}.
     * <p>
     * Unlike using JDK {@link java.io.PipedInputStream} and {@link java.io.PipedOutputStream} for this, this
     * solution works safely in a single thread environment.
     * </p>
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     * outputStream.writeBytes("hello world".getBytes(StandardCharsets.UTF_8));
     *
     * InputStream inputStream = IOUtils.copy(outputStream);
     * </pre>
     *
     * @param outputStream the {@code java.io.ByteArrayOutputStream} to read from, not {@code null} or closed.
     * @return the {@code QueueInputStream} filled with the content of the {@code java.io.ByteArrayOutputStream}.
     * @throws NullPointerException if the {@code java.io.ByteArrayOutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.12
     */
    @SuppressWarnings("resource") // streams are closed by the caller.
    public static QueueInputStream copy(final java.io.ByteArrayOutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream");
        final QueueInputStream in = new QueueInputStream();
        outputStream.writeTo(in.newQueueOutputStream());
        return in;
    }

    /**
     * Copies chars from a {@link Reader} to a {@link Appendable}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * Large streams (over 2GB) will return a chars copied value of
     * {@code -1} after the copy has completed since the correct
     * number of chars cannot be returned as an int. For large streams
     * use the {@link #copyLarge(Reader, Writer)} method.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param output the {@code Appendable} to write to, not {@code null} or closed.
     * @return the number of characters copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Appendable} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.7
     */
    public static long copy(final Reader reader, final Appendable output) throws IOException {
        return copy(reader, output, CharBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    /**
     * Copies chars from a {@link Reader} to an {@link Appendable}.
     * <p>
     * This method uses the provided {@link CharBuffer}, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param output the {@code Appendable} to write to, not {@code null} or closed.
     * @param buffer the {@code CharBuffer} to be used for the copy, not {@code null}.
     * @return the number of characters copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Appendable} is {@code null}.
     * @throws NullPointerException if the {@code CharBuffer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.7
     */
    public static long copy(final Reader reader, final Appendable output, final CharBuffer buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            buffer.flip();
            output.append(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copies chars from a {@link Reader} to bytes on an
     * {@link OutputStream} using the {@link Charset#defaultCharset()} for encoding,
     * and calling {@link Flushable#flush()}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * Due to the implementation of {@link OutputStreamWriter}, this method performs a
     * {@code Flushable#flush()}.
     * </p>
     * <p>
     * This method uses {@code OutputStreamWriter}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param output the {@code OutputStream} to write to, not {@code null} or closed.

     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #copy(Reader, OutputStream, Charset)} instead.
     */
    @Deprecated
    public static void copy(final Reader reader, final OutputStream output)
            throws IOException {
        copy(reader, output, Charset.defaultCharset());
    }

    /**
     * Copies chars from a {@link Reader} to bytes on an
     * {@link OutputStream} using the specified character encoding, and
     * calling {@link Flushable#flush()}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * Due to the implementation of {@link OutputStreamWriter}, this method performs a
     * {@code Flushable#flush()}.
     * </p>
     * <p>
     * This method uses {@code OutputStreamWriter}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param output the {@code OutputStream} to write to, not {@code null} or closed.
     * @param outputCharset the {@link Charset} to use for the {@code OutputStream}, {@code null} uses {@link Charset#defaultCharset()}.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.3
     */
    public static void copy(final Reader reader, final OutputStream output, final Charset outputCharset)
            throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.toCharset(outputCharset));
        copy(reader, writer);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter,
        // we have to flush here.
        writer.flush();
    }

    /**
     * Copies chars from a {@link Reader} to bytes on an
     * {@link OutputStream} using the specified character encoding, and
     * calling {@link Flushable#flush()}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * Due to the implementation of {@link OutputStreamWriter}, this method performs a
     * flush.
     * </p>
     * <p>
     * This method uses {@code OutputStreamWriter}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param output the {@code OutputStream} to write to, not {@code null} or closed.
     * @param outputCharsetName the name of the requested {@link Charset} for the {@code OutputStream},
     * {@code null} uses {@link Charset#defaultCharset()}.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void copy(final Reader reader, final OutputStream output, final String outputCharsetName)
            throws IOException {
        copy(reader, output, Charsets.toCharset(outputCharsetName));
    }

    /**
     * Copies chars from a {@link Reader} to a {@link Writer}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * Large streams (over 2GB) will return a chars copied value of
     * {@code -1} after the copy has completed since the correct
     * number of chars cannot be returned as an int. For large streams
     * use the {@link #copyLarge(Reader, Writer)} method.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @return the number of characters copied, or -1 if greater than {@link Integer#MAX_VALUE}.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.1
     */
    public static int copy(final Reader reader, final Writer writer) throws IOException {
        final long count = copyLarge(reader, writer);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

    /**
     * Copies bytes from a {@link URL} to a {@link File}.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param url the {@code URL} to read from, not {@code null}.
     * @param file the {@code File} to write to, not {@code null}.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code URL} is {@code null}.
     * @throws NullPointerException if the {@code File} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long copy(final URL url, final File file) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(Objects.requireNonNull(file, "file").toPath())) {
            return copy(url, outputStream);
        }
    }

    /**
     * Copies bytes from a {@link URL} to an {@link OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param url the {@code URL} to read from, not {@code null}.
     * @param outputStream the {@code OutputStream} to write to, not {@code null} or closed.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code URL} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long copy(final URL url, final OutputStream outputStream) throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(url, "url").openStream()) {
            return copyLarge(inputStream, outputStream);
        }
    }

    /**
     * Copies bytes from a large (over 2GB) {@link InputStream} to an
     * {@link OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null} or closed.
     * @param outputStream the {@code OutputStream} to write to, not {@code null} or closed.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 1.3
     */
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream)
            throws IOException {
        return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from a large (over 2GB) {@link InputStream} to an
     * {@link OutputStream}.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null} or closed.
     * @param outputStream the {@code OutputStream} to write to, not {@code null} or closed.
     * @param buffer the {@code byte[]} buffer to use for the copy.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.2
     */
    @SuppressWarnings("resource") // streams are closed by the caller.
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer)
        throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(outputStream, "outputStream");
        long count = 0L;
        int n;
        while ((n = inputStream.read(buffer)) > EOF) {
            outputStream.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copies some or all bytes from a large (over 2GB) {@link InputStream} to an
     * {@link OutputStream}, optionally skipping input bytes.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * Note that the implementation uses {@link #skip(InputStream, long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of characters are skipped.
     * </p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param output the {@code OutputStream} to write to, not {@code null} or closed.
     * @param inputOffset : number of bytes to skip from input before copying.
     * -ve values are ignored.
     * @param length : number of bytes to copy. -ve means all.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.2
     */
    public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
                                 final long length) throws IOException {
        return copyLarge(input, output, inputOffset, length, getByteArray());
    }

    /**
     * Copies some or all bytes from a large (over 2GB) {@link InputStream} to an
     * {@link OutputStream}, optionally skipping input bytes.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     * <p>
     * Note that the implementation uses {@link #skip(InputStream, long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of characters are skipped.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param output the {@code OutputStream} to write to, not {@code null} or closed.
     * @param inputOffset : number of bytes to skip from input before copying.
     * -ve values are ignored.
     * @param length : number of bytes to copy. -ve means all.
     * @param buffer the {@code byte[]} buffer to use for the copy.
     * @return the number of bytes copied.
     * @throws NullPointerException if the {@code InputStream} is {@code null}.
     * @throws NullPointerException if the {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.2
     */
    public static long copyLarge(final InputStream input, final OutputStream output,
                                 final long inputOffset, final long length, final byte[] buffer) throws IOException {
        if (inputOffset > 0) {
            skipFully(input, inputOffset);
        }
        if (length == 0) {
            return 0;
        }
        final int bufferLength = buffer.length;
        int bytesToRead = bufferLength;
        if (length > 0 && length < bufferLength) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += read;
            if (length > 0) { // only adjust length if not reading to the end
                // Note the cast must work because buffer.length is an integer
                bytesToRead = (int) Math.min(length - totalRead, bufferLength);
            }
        }
        return totalRead;
    }

    /**
     * Copies chars from a large (over 2GB) {@link Reader} to a {@link Writer}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @return the number of characters copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.3
     */
    public static long copyLarge(final Reader reader, final Writer writer) throws IOException {
        return copyLarge(reader, writer, getCharArray());
    }

    /**
     * Copies chars from a large (over 2GB) {@link Reader} to a {@link Writer}.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @param buffer the {@code char[]} buffer to be used for the copy.
     * @return the number of characters copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.2
     */
    public static long copyLarge(final Reader reader, final Writer writer, final char[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copies some or all chars from a large (over 2GB) {@link Reader} to an
     * {@link Writer}, optionally skipping input chars.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @param inputOffset : number of chars to skip from input before copying.
     * -ve values are ignored.
     * @param length : number of chars to copy. -ve means all.
     * @return the number of chars copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.2
     */
    public static long copyLarge(final Reader reader, final Writer writer, final long inputOffset, final long length)
            throws IOException {
        return copyLarge(reader, writer, inputOffset, length, getCharArray());
    }

    /**
     * Copies some or all chars from a large (over 2GB) {@link Reader} to an
     * {@link Writer}, optionally skipping input chars.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null} or closed.
     * @param writer the {@code Writer} to write to, not {@code null} or closed.
     * @param inputOffset : number of chars to skip from input before copying.
     * -ve values are ignored.
     * @param length : number of chars to copy. -ve means all.
     * @param buffer the {@code char[]} buffer to be used for the copy.
     * @return the number of chars copied.
     * @throws NullPointerException if the {@code Reader} is {@code null}.
     * @throws NullPointerException if the {@code Writer} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.2
     */
    public static long copyLarge(final Reader reader, final Writer writer, final long inputOffset, final long length,
                                 final char[] buffer)
            throws IOException {
        if (inputOffset > 0) {
            skipFully(reader, inputOffset);
        }
        if (length == 0) {
            return 0;
        }
        int bytesToRead = buffer.length;
        if (length > 0 && length < buffer.length) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && EOF != (read = reader.read(buffer, 0, bytesToRead))) {
            writer.write(buffer, 0, read);
            totalRead += read;
            if (length > 0) { // only adjust length if not reading to the end
                // Note the cast must work because buffer.length is an integer
                bytesToRead = (int) Math.min(length - totalRead, buffer.length);
            }
        }
        return totalRead;
    }

    /**
     * Gets the thread local byte array.
     *
     * @return the thread local byte array.
     */
    static byte[] getByteArray() {
        return SKIP_BYTE_BUFFER.get();
    }

    /**
     * Gets the thread local char array.
     *
     * @return the thread local char array.
     */
    static char[] getCharArray() {
        return SKIP_CHAR_BUFFER.get();
    }

    /**
     * Returns the length of the given {@code byte[]} array in a {@code null}-safe manner.
     *
     * @param array a {@code byte[]} array or {@code null}.
     * @return the array length -- or 0 if the given array is {@code null}.
     * @since 2.7
     */
    public static int length(final byte[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the length of the given {@code char[]} array in a {@code null}-safe manner.
     *
     * @param array a {@code char[]} array or {@code null}.
     * @return the array length -- or 0 if the given array is {@code null}.
     * @since 2.7
     */
    public static int length(final char[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the length of the given {@link CharSequence} in a {@code null}-safe manner.
     *
     * @param csq a {@code CharSequence} or {@code null}.
     * @return the {@code CharSequence} length -- or 0 if the given {@code CharSequence} is {@code null}.
     * @since 2.7
     */
    public static int length(final CharSequence csq) {
        return csq == null ? 0 : csq.length();
    }

    /**
     * Returns the length of the given {@code Object[]} array in a {@code null}-safe manner.
     *
     * @param array a {@code Object[]} array or {@code null}.
     * @return the array length -- or 0 if the given {@code Object[]} array is {@code null}.
     * @since 2.7
     */
    public static int length(final Object[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns a {@link LineIterator} for the lines in an {@link InputStream}, using
     * the {@link Charset} specified (or {@link Charset#defaultCharset()} if {@code null}).
     * <p>
     * {@code LineIterator} holds a reference to the open
     * {@code InputStream} specified here. When you have finished with
     * the iterator you should close the stream to free internal resources.
     * This can be done by using a {@code try-with-resources} block, closing the stream directly, or by calling
     * {@link LineIterator#close()}.
     * </p>
     * <p>
     * The recommended usage pattern is:
     * </p>
     * <pre>
     * try {
     *   LineIterator it = IOUtils.lineIterator(stream, charset);
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   IOUtils.closeQuietly(stream);
     * }
     * </pre>
     *
     * @param input the {@code InputStream} to read from, not {@code null} or closed.
     * @param charset the {@code CharSet} to use, {@code null} means {@code Charset#defaultCharset()}.
     * @return a {@code LineIterator} of the lines in the {@code InputStream}, never {@code null}.
     * @throws IllegalArgumentException if the {@code InputStream} is {@code null}.
     * @since 2.3
     */
    public static LineIterator lineIterator(final InputStream input, final Charset charset) {
        return new LineIterator(new InputStreamReader(input, Charsets.toCharset(charset)));
    }

    /**
     * Returns an Iterator for the lines in an {@code InputStream}, using
     * the character encoding specified (or default encoding if null).
     * <p>
     * {@code LineIterator} holds a reference to the open
     * {@code InputStream} specified here. When you have finished with
     * the iterator you should close the stream to free internal resources.
     * This can be done by using a try-with-resources block, closing the stream directly, or by calling
     * {@link LineIterator#close()}.
     * </p>
     * <p>
     * The recommended usage pattern is:
     * </p>
     * <pre>
     * try {
     *   LineIterator it = IOUtils.lineIterator(stream, "UTF-8");
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   IOUtils.closeQuietly(stream);
     * }
     * </pre>
     *
     * @param input the {@code InputStream} to read from, not null
     * @param charsetName the encoding to use, null means platform default
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException                     if the input is null
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.2
     */
    public static LineIterator lineIterator(final InputStream input, final String charsetName) {
        return lineIterator(input, Charsets.toCharset(charsetName));
    }

    /**
     * Returns an Iterator for the lines in a {@code Reader}.
     * <p>
     * {@code LineIterator} holds a reference to the open
     * {@code Reader} specified here. When you have finished with the
     * iterator you should close the reader to free internal resources.
     * This can be done by using a try-with-resources block, closing the reader directly, or by calling
     * {@link LineIterator#close()}.
     * </p>
     * <p>
     * The recommended usage pattern is:
     * </p>
     * <pre>
     * try {
     *   LineIterator it = IOUtils.lineIterator(reader);
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   IOUtils.closeQuietly(reader);
     * }
     * </pre>
     *
     * @param reader the {@code Reader} to read from, not null
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException if the reader is null
     * @since 1.2
     */
    public static LineIterator lineIterator(final Reader reader) {
        return new LineIterator(reader);
    }

    /**
     * Reads bytes from an input stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link InputStream}.
     *
     * @param input where to read input from
     * @param buffer destination
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final InputStream input, final byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from an input stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link InputStream}.
     *
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = input.read(buffer, offset + location, remaining);
            if (EOF == count) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    /**
     * Reads bytes from a ReadableByteChannel.
     * <p>
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link ReadableByteChannel}.
     * </p>
     *
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @return the actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.5
     */
    public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int length = buffer.remaining();
        while (buffer.remaining() > 0) {
            final int count = input.read(buffer);
            if (EOF == count) { // EOF
                break;
            }
        }
        return length - buffer.remaining();
    }

    /**
     * Reads characters from an input character stream.
     * This implementation guarantees that it will read as many characters
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link Reader}.
     *
     * @param reader where to read input from
     * @param buffer destination
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final Reader reader, final char[] buffer) throws IOException {
        return read(reader, buffer, 0, buffer.length);
    }

    /**
     * Reads characters from an input character stream.
     * This implementation guarantees that it will read as many characters
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link Reader}.
     *
     * @param reader where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = reader.read(buffer, offset + location, remaining);
            if (EOF == count) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param input where to read input from
     * @param buffer destination
     *
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.2
     */
    public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
        readFully(input, buffer, 0, buffer.length);
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     *
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.2
     */
    public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param input where to read input from
     * @param length length to read, must be &gt;= 0
     * @return the bytes read from input
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.5
     */
    public static byte[] readFully(final InputStream input, final int length) throws IOException {
        final byte[] buffer = IOUtils.byteArray(length);
        readFully(input, buffer, 0, buffer.length);
        return buffer;
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link ReadableByteChannel#read(ByteBuffer)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @throws IOException  if there is a problem reading the file
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.5
     */
    public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int expected = buffer.remaining();
        final int actual = read(input, buffer);
        if (actual != expected) {
            throw new EOFException("Length to read: " + expected + " actual: " + actual);
        }
    }

    /**
     * Reads the requested number of characters or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link Reader#read(char[], int, int)} may
     * not read as many characters as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param reader where to read input from
     * @param buffer destination
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of characters read was incorrect
     * @since 2.2
     */
    public static void readFully(final Reader reader, final char[] buffer) throws IOException {
        readFully(reader, buffer, 0, buffer.length);
    }

    /**
     * Reads the requested number of characters or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link Reader#read(char[], int, int)} may
     * not read as many characters as requested (most likely because of reaching EOF).
     * </p>
     *
     * @param reader where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of characters read was incorrect
     * @since 2.2
     */
    public static void readFully(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(reader, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #readLines(InputStream, Charset)} instead
     */
    @Deprecated
    public static List<String> readLines(final InputStream input) throws IOException {
        return readLines(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not null
     * @param charset the charset to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static List<String> readLines(final InputStream input, final Charset charset) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(charset));
        return readLines(reader);
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not null
     * @param charsetName the name of the requested charset, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException                         if the input is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static List<String> readLines(final InputStream input, final String charsetName) throws IOException {
        return readLines(input, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@code Reader} as a list of Strings,
     * one entry per line.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    @SuppressWarnings("resource") // reader wraps input and is the responsibility of the caller.
    public static List<String> readLines(final Reader reader) throws IOException {
        final BufferedReader bufReader = toBufferedReader(reader);
        final List<String> list = new ArrayList<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            list.add(line);
        }
        return list;
    }

    /**
     * Gets the contents of a classpath resource as a byte array.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name name of the desired resource
     * @return the requested byte array
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static byte[] resourceToByteArray(final String name) throws IOException {
        return resourceToByteArray(name, null);
    }

    /**
     * Gets the contents of a classpath resource as a byte array.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name name of the desired resource
     * @param classLoader the class loader that the resolution of the resource is delegated to
     * @return the requested byte array
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static byte[] resourceToByteArray(final String name, final ClassLoader classLoader) throws IOException {
        return toByteArray(resourceToURL(name, classLoader));
    }

    /**
     * Gets the contents of a classpath resource as a String using the
     * specified character encoding.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name     name of the desired resource
     * @param charset the charset to use, null means platform default
     * @return the requested String
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static String resourceToString(final String name, final Charset charset) throws IOException {
        return resourceToString(name, charset, null);
    }

    /**
     * Gets the contents of a classpath resource as a String using the
     * specified character encoding.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name     name of the desired resource
     * @param charset the charset to use, null means platform default
     * @param classLoader the class loader that the resolution of the resource is delegated to
     * @return the requested String
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static String resourceToString(final String name, final Charset charset, final ClassLoader classLoader) throws IOException {
        return toString(resourceToURL(name, classLoader), charset);
    }

    /**
     * Gets a URL pointing to the given classpath resource.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name name of the desired resource
     * @return the requested URL
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static URL resourceToURL(final String name) throws IOException {
        return resourceToURL(name, null);
    }

    /**
     * Gets a URL pointing to the given classpath resource.
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name        name of the desired resource
     * @param classLoader the class loader that the resolution of the resource is delegated to
     * @return the requested URL
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
        // What about the thread context class loader?
        // What about the system class loader?
        final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }
        return resource;
    }

    /**
     * Skips bytes from an input byte stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * skip() implementations in subclasses of {@link InputStream}.
     * <p>
     * Note that the implementation uses {@link InputStream#read(byte[], int, int)} rather
     * than delegating to {@link InputStream#skip(long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of bytes are skipped.
     * </p>
     *
     * @param input byte stream to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see InputStream#skip(long)
     * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add skipFully() method for InputStreams</a>
     * @since 2.0
     */
    public static long skip(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        /*
         * N.B. no need to synchronize access to SKIP_BYTE_BUFFER: - we don't care if the buffer is created multiple
         * times (the data is ignored) - we always use the same size buffer, so if it it is recreated it will still be
         * OK (if the buffer size were variable, we would need to synch. to ensure some other thread did not create a
         * smaller one)
         */
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final byte[] byteArray = getByteArray();
            final long n = input.read(byteArray, 0, (int) Math.min(remain, byteArray.length));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    /**
     * Skips bytes from a ReadableByteChannel.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up.
     *
     * @param input ReadableByteChannel to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException              if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @since 2.5
     */
    public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        final ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, DEFAULT_BUFFER_SIZE));
        long remain = toSkip;
        while (remain > 0) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int) Math.min(remain, DEFAULT_BUFFER_SIZE));
            final int n = input.read(skipByteBuffer);
            if (n == EOF) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    /**
     * Skips characters from an input character stream.
     * This implementation guarantees that it will read as many characters
     * as possible before giving up; this may not always be the case for
     * skip() implementations in subclasses of {@link Reader}.
     * <p>
     * Note that the implementation uses {@link Reader#read(char[], int, int)} rather
     * than delegating to {@link Reader#skip(long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of characters are skipped.
     * </p>
     *
     * @param reader character stream to skip
     * @param toSkip number of characters to skip.
     * @return number of characters actually skipped.
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see Reader#skip(long)
     * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add skipFully() method for InputStreams</a>
     * @since 2.0
     */
    public static long skip(final Reader reader, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final char[] charArray = getCharArray();
            final long n = reader.read(charArray, 0, (int) Math.min(remain, charArray.length));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    /**
     * Skips the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#skip(long)} may
     * not skip as many bytes as requested (most likely because of reaching EOF).
     * </p>
     * <p>
     * Note that the implementation uses {@link #skip(InputStream, long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of characters are skipped.
     * </p>
     *
     * @param input stream to skip
     * @param toSkip the number of bytes to skip
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException             if the number of bytes skipped was incorrect
     * @see InputStream#skip(long)
     * @since 2.0
     */
    public static void skipFully(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    /**
     * Skips the requested number of bytes or fail if there are not enough left.
     *
     * @param input ReadableByteChannel to skip
     * @param toSkip the number of bytes to skip
     * @throws IOException              if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException             if the number of bytes skipped was incorrect
     * @since 2.5
     */
    public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    /**
     * Skips the requested number of characters or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link Reader#skip(long)} may
     * not skip as many characters as requested (most likely because of reaching EOF).
     * </p>
     * <p>
     * Note that the implementation uses {@link #skip(Reader, long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of characters are skipped.
     * </p>
     *
     * @param reader stream to skip
     * @param toSkip the number of characters to skip
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException             if the number of characters skipped was incorrect
     * @see Reader#skip(long)
     * @since 2.0
     */
    public static void skipFully(final Reader reader, final long toSkip) throws IOException {
        final long skipped = skip(reader, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
        }
    }

    /**
     * Fetches entire contents of an {@code InputStream} and represent
     * same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for
     * long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * <p>
     * It can be used in favor of {@link #toByteArray(InputStream)}, since it
     * avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param input Stream to be fully buffered.
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    public static InputStream toBufferedInputStream(final InputStream input) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input);
    }

    /**
     * Fetches entire contents of an {@code InputStream} and represent
     * same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for
     * long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * <p>
     * It can be used in favor of {@link #toByteArray(InputStream)}, since it
     * avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param input Stream to be fully buffered.
     * @param size the initial buffer size
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     * @since 2.5
     */
    public static InputStream toBufferedInputStream(final InputStream input, final int size) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input, size);
    }

    /**
     * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a BufferedReader from the given
     * reader.
     *
     * @param reader the reader to wrap or return (not null)
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @throws NullPointerException if the input parameter is null
     * @see #buffer(Reader)
     * @since 2.2
     */
    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Returns the given {@link Reader} if it is a {@link BufferedReader}, otherwise creates a
     * {@code BufferedReader} from the given {@code Reader}.
     *
     * @param reader the {@code Reader} to wrap or return, not {@code null}, not closed.
     * @param size the buffer size, if a new {@code BufferedReader} is created.
     * @return the given {@code Reader} or a new {@code BufferedReader} for the given {@code Reader}.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @see #buffer(Reader)
     * @since 2.5
     */
    public static BufferedReader toBufferedReader(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    /**
     * Gets the contents of an {@code InputStream} as a {@code byte[]}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read.
     * @return the requested byte array.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws IOException if an I/O error occurs or reading more than {@link Integer#MAX_VALUE} occurs.
     */
    public static byte[] toByteArray(final InputStream inputStream) throws IOException {
        // We use a ThresholdingOutputStream to avoid reading AND writing more than Integer.MAX_VALUE.
        try (final UnsynchronizedByteArrayOutputStream ubaOutput = new UnsynchronizedByteArrayOutputStream();
            final ThresholdingOutputStream thresholdOuput = new ThresholdingOutputStream(Integer.MAX_VALUE, os -> {
                throw new IllegalArgumentException(
                    String.format("Cannot read more than %,d into a byte array", Integer.MAX_VALUE));
            }, os -> ubaOutput)) {
            copy(inputStream, thresholdOuput);
            return ubaOutput.toByteArray();
        }
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@code byte[]}. Use this method instead of
     * {@link #toByteArray(InputStream)} when {@code InputStream} size is known.
     *
     * @param input the {@code InputStream} to read, not {@code null}, not closed.
     * @param size the size of {@code InputStream}.
     * @return the requested {@code byte[]} array.
     * @throws IOException if an I/O error occurs or {@code InputStream} size differ from parameter size.
     * @throws IllegalArgumentException if size is less than zero.
     * @since 2.1
     */
    public static byte[] toByteArray(final InputStream input, final int size) throws IOException {

        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }

        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        final byte[] data = IOUtils.byteArray(size);
        int offset = 0;
        int read;

        while (offset < size && (read = input.read(data, offset, size - offset)) != EOF) {
            offset += read;
        }

        if (offset != size) {
            throw new IOException("Unexpected read size, current: " + offset + ", expected: " + size);
        }

        return data;
    }

    /**
     * Gets contents of an {@code InputStream} as a {@code byte[]}.
     * Use this method instead of {@code toByteArray(InputStream)}
     * when {@code InputStream} size is known.
     * <b>NOTE:</b> the method checks that the length can safely be cast to an int without truncation
     * before using {@link IOUtils#toByteArray(java.io.InputStream, int)} to read into the byte array.
     * (Arrays can have no more than Integer.MAX_VALUE entries anyway)
     *
     * @param input the {@code InputStream} to read from
     * @param size the size of {@code InputStream}
     * @return the requested byte array
     * @throws IOException              if an I/O error occurs or {@code InputStream} size differ from parameter
     * size
     * @throws IllegalArgumentException if size is less than zero or size is greater than Integer.MAX_VALUE.
     * @see IOUtils#toByteArray(java.io.InputStream, int)
     * @since 2.1
     */
    public static byte[] toByteArray(final InputStream input, final long size) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }
        return toByteArray(input, (int) size);
    }

    /**
     * Gets the contents of a {@link Reader} as a {@code byte[]}
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null}, not closed.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @deprecated 2.5 use {@link #toByteArray(Reader, Charset)} instead.
     */
    @Deprecated
    public static byte[] toByteArray(final Reader reader) throws IOException {
        return toByteArray(reader, Charset.defaultCharset());
    }

    /**
     * Gets the contents of a {@link Reader} as a {@code byte[]}
     * using the specified {@link Charset} for encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.3
     */
    public static byte[] toByteArray(final Reader reader, final Charset charset) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(reader, output, charset);
            return output.toByteArray();
        }
    }

    /**
     * Gets the contents of a {@link Reader} as a {@code byte[]}
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@link Reader} to read from, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException                         if the given {@code Reader} is {@code null}.
     * @throws IOException                                  if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static byte[] toByteArray(final Reader reader, final String charsetName) throws IOException {
        return toByteArray(reader, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@code String} as a {@code byte[]}
     * using the default character encoding of the platform.
     * <p>
     * This is the same as {@link String#getBytes()}.
     * </p>
     *
     * @param input the {@code String} to convert, not {@code null}.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code String} is {@code null}.
     * @deprecated 2.5 Use {@link String#getBytes()} instead.
     */
    @Deprecated
    public static byte[] toByteArray(final String input) {
        // make explicit the use of the default charset
        return input.getBytes(Charset.defaultCharset());
    }

    /**
     * Gets the contents of a {@link URI} as a {@code byte[]}.
     *
     * @param uri the {@code URI} to read, not {@code null}.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code URI} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.4
     */
    public static byte[] toByteArray(final URI uri) throws IOException {
        return IOUtils.toByteArray(uri.toURL());
    }

    /**
     * Gets the contents of a {@link URL} as a {@code byte[]}.
     *
     * @param url the {@code URL} to read, not {@code null}.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code URL} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.4
     */
    public static byte[] toByteArray(final URL url) throws IOException {
        try (final CloseableURLConnection urlConnection = CloseableURLConnection.open(url)) {
            return IOUtils.toByteArray(urlConnection);
        }
    }

    /**
     * Gets the contents of a {@link URLConnection} as a {@code byte[]}.
     *
     * @param urlConnection the {@code URLConnection} to read, not {@code null}.
     * @return the requested {@code byte[]} array.
     * @throws NullPointerException if the given {@code URLConnection} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.4
     */
    public static byte[] toByteArray(final URLConnection urlConnection) throws IOException {
        try (InputStream inputStream = urlConnection.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@code char[]} array
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null}, not closed.
     * @return the requested {@code char[]} array.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #toCharArray(InputStream, Charset)} instead.
     */
    @Deprecated
    public static char[] toCharArray(final InputStream inputStream) throws IOException {
        return toCharArray(inputStream, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@code char[]} array
     * using the specified {@link Charset} for encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @return the requested {@code char[]} array.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.3
     */
    public static char[] toCharArray(final InputStream inputStream, final Charset charset)
            throws IOException {
        final CharArrayWriter writer = new CharArrayWriter();
        copy(inputStream, writer, charset);
        return writer.toCharArray();
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@code char[]} array
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read from, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return the requested {@code char[]} array.
     * @throws NullPointerException                         if the given {@code InputStream} is {@code null}.
     * @throws IOException                                  if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static char[] toCharArray(final InputStream inputStream, final String charsetName) throws IOException {
        return toCharArray(inputStream, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@link Reader} as a {@code char[]} array.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null}, not closed.
     * @return the requested {@code char[]} array.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 1.1
     */
    public static char[] toCharArray(final Reader reader) throws IOException {
        final CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toCharArray();
    }

    /**
     * Converts the specified {@link CharSequence} to an {@link InputStream}, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the {@code CharSequence} to convert, not {@code null}.
     * @return an {@code InputStream} representing the bytes of the {@code CharSequence}.
     * @throws NullPointerException if the given {@code CharSequence} is {@code null}.
     * @since 2.0
     * @deprecated 2.5 use {@link #toInputStream(CharSequence, Charset)} instead.
     */
    @Deprecated
    public static InputStream toInputStream(final CharSequence input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    /**
     * Converts the specified {@link CharSequence} to an {@link InputStream}, encoded as bytes
     * using the specified {@link Charset} for encoding.
     *
     * @param input the {@code CharSequence} to convert, not {@code null}.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @return an {@code InputStream} representing the bytes of the {@code CharSequence}.
     * @throws NullPointerException if the given {@code CharSequence} is {@code null}.
     * @since 2.3
     */
    public static InputStream toInputStream(final CharSequence input, final Charset charset) {
        return toInputStream(input.toString(), charset);
    }

    /**
     * Converts the specified {@link CharSequence} to an {@link InputStream}, encoded as bytes
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     *
     * @param input the {@code CharSequence} to convert, not {@code null}.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return an {@code InputStream} representing the bytes of the {@code CharSequence}.
     * @throws NullPointerException if the given {@code CharSequence} is {@code null}.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 2.0
     */
    public static InputStream toInputStream(final CharSequence input, final String charsetName) {
        return toInputStream(input, Charsets.toCharset(charsetName));
    }

    /**
     * Converts the specified {code String} to an {@link InputStream}, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the {@code String} to convert, not {@code null}.
     * @return an {@code InputStream} representing the bytes of the {@code String}.
     * @throws NullPointerException if the given {@code String} is {@code null}.
     * @since 1.1
     * @deprecated 2.5 use {@link #toInputStream(String, Charset)} instead.
     */
    @Deprecated
    public static InputStream toInputStream(final String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    /**
     * Converts the specified {code String} to an {@link InputStream}, encoded as bytes
     * using the specified {@link Charset} for encoding.
     *
     * @param input the {@code String} to convert, not {@code null}.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @return an {@code InputStream} representing the bytes of the {@code String}.
     * @throws NullPointerException if the given {@code String} is {@code null}.
     * @since 2.3
     */
    public static InputStream toInputStream(final String input, final Charset charset) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charset)));
    }

    /**
     * Converts the specified {code String} to an {@link InputStream}, encoded as bytes
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     *
     * @param input the {@code String} to convert, not {@code null}.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return an {@code InputStream} representing the bytes of the {@code String}.
     * @throws NullPointerException if the given {@code String} is {@code null}.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static InputStream toInputStream(final String input, final String charsetName) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charsetName)));
    }

    /**
     * Gets the contents of a {@code byte[]} as a {@code String}
     * using the default character encoding of the platform.
     *
     * @param input the {@code byte[]} array to read from, not {@code null}.
     * @return the requested {@code String}.
     * @throws NullPointerException if the given {@code byte[]} is {@code null}.
     * @deprecated 2.5 Use {@link String#String(byte[])} instead.
     */
    @Deprecated
    public static String toString(final byte[] input) {
        // make explicit the use of the default charset
        return new String(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of a {@code byte[]} as a {@code String}
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     *
     * @param input the {code byte[]} array to read from, not {@code null}.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return the requested {@code String}.
     * @throws NullPointerException if the given {@code byte[]} is {@code null}.
     */
    public static String toString(final byte[] input, final String charsetName) {
        return new String(input, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@link String}
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null}, not closed.
     * @return the requested {@code String}.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @deprecated 2.5 use {@link #toString(InputStream, Charset)} instead.
     */
    @Deprecated
    public static String toString(final InputStream input) throws IOException {
        return toString(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@link String}
     * using the specified {@link Charset} encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @return the requested {@code String}.
     * @throws NullPointerException if the given {@code InputStream} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.3
     */
    public static String toString(final InputStream input, final Charset charset) throws IOException {
        try (final StringBuilderWriter sw = new StringBuilderWriter()) {
            copy(input, sw, charset);
            return sw.toString();
        }
    }

    /**
     * Gets the contents of an {@link InputStream} as a {@link String}
     * using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedInputStream}.
     * </p>
     *
     * @param input the {@code InputStream} to read from, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @return the requested {@code String}.
     * @throws NullPointerException                         if the given {@code InputStream} is {@code null}.
     * @throws IOException                                  if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     */
    public static String toString(final InputStream input, final String charsetName)
            throws IOException {
        return toString(input, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@link Reader} as a {@link String}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@link BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from, not {@code null}, not closed.
     * @return the requested {@code String}.
     * @throws NullPointerException if the given {@code Reader} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     */
    public static String toString(final Reader reader) throws IOException {
        try (final StringBuilderWriter sw = new StringBuilderWriter()) {
            copy(reader, sw);
            return sw.toString();
        }
    }

    /**
     * Gets the contents at the given {@link URI}.
     *
     * @param uri The {@code URI} source.
     * @return The contents of the {@code URI} as a {@link String}.
     * @throws IOException          if an I/O exception occurs.
     * @throws NullPointerException if the given {@code URI} is {@code null}.
     * @since 2.1
     * @deprecated 2.5 use {@link #toString(URI, Charset)} instead
     */
    @Deprecated
    public static String toString(final URI uri) throws IOException {
        return toString(uri, Charset.defaultCharset());
    }

    /**
     * Gets the contents at the given {@link URI}.
     *
     * @param uri The {@code URI} source.
     * @param encoding The {@link Charset} for the {@code URI} contents.
     * @return The contents of the {@code URI} as a {@link String}.
     * @throws IOException          if an I/O exception occurs.
     * @throws NullPointerException if the given {@code URI} is {@code null}.
     * @since 2.3.
     */
    public static String toString(final URI uri, final Charset encoding) throws IOException {
        return toString(uri.toURL(), Charsets.toCharset(encoding));
    }

    /**
     * Gets the contents at the given {@link URI}.
     *
     * @param uri The {@code URI} source.
     * @param charsetName The encoding name for the URL contents.
     * @return The contents of the {@code URI} as a {link String}.
     * @throws IOException                                  if an I/O exception occurs.
     * @throws NullPointerException                         if the given {@code URI} is {@code null}.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 2.1
     */
    public static String toString(final URI uri, final String charsetName) throws IOException {
        return toString(uri, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents at the given {@link URL}.
     *
     * @param url The {@code URL} source.
     * @return The contents of the {@code URL} as a {@link String}.
     * @throws IOException          if an I/O exception occurs.
     * @throws NullPointerException if the given {@code URL} is {@code null}.
     * @since 2.1
     * @deprecated 2.5 use {@link #toString(URL, Charset)} instead.
     */
    @Deprecated
    public static String toString(final URL url) throws IOException {
        return toString(url, Charset.defaultCharset());
    }

    /**
     * Gets the contents at the given {@link URL}.
     *
     * @param url The {@code URL} source.
     * @param encoding The {@link Charset} for the {@code URL} contents.
     * @return The contents of the {@code URL} as a {@link String}.
     * @throws IOException          if an I/O exception occurs.
     * @throws NullPointerException if the given {@code URL} is {@code null}.
     * @since 2.3
     */
    public static String toString(final URL url, final Charset encoding) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return toString(inputStream, encoding);
        }
    }

    /**
     * Gets the contents at the given {@link URL}.
     *
     * @param url The {@code URL} source.
     * @param charsetName The encoding name of the requested {@link Charset}
     * for the {@code URL} contents, {@code null} means platform default.
     * @return The contents of the {@code URL} as a {@link String}.
     * @throws IOException                                  if an I/O exception occurs.
     * @throws NullPointerException                         if the given {@code URL} is {@code null}.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 2.1
     */
    public static String toString(final URL url, final String charsetName) throws IOException {
        return toString(url, Charsets.toCharset(charsetName));
    }

    /**
     * Writes bytes from a {@code byte[]} to an {@link OutputStream}.
     *
     * @param data the {@code byte[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     */
    public static void write(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@link Writer}
     * using the default character encoding of the platform.
     * <p>
     * This method uses {@link String#String(byte[])}.
     * </p>
     *
     * @param data the {@code byte[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #write(byte[], Writer, Charset)} instead
     */
    @Deprecated
    public static void write(final byte[] data, final Writer writer) throws IOException {
        write(data, writer, Charset.defaultCharset());
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@link Writer}
     * using the specified character encoding.
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     * </p>
     *
     * @param data the {@code byte[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @param charset the {@link Charset} to use, {@code null} means platform default.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.3
     */
    public static void write(final byte[] data, final Writer writer, final Charset charset) throws IOException {
        if (data != null) {
            writer.write(new String(data, Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@link Writer}
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     * </p>
     *
     * @param data the {@code byte[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@link Charset}, {@code null} means platform default.
     * @throws NullPointerException                         if the given {@code Writer} is {@code null}.
     * @throws IOException                                  if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void write(final byte[] data, final Writer writer, final String charsetName) throws IOException {
        write(data, writer, Charsets.toCharset(charsetName));
    }

    /**
     * Writes all characters from a {@code char[]} to bytes on an
     * {@link OutputStream}.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes()}.
     * </p>
     *
     * @param data the {@code char[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #write(char[], OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final char[] data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes all characters from a {@code char[]} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} encoding.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code char[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.3
     */
    public static void write(final char[] data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes all characters from a {@code char[]} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code char[]} array to write, do not modify during output,
     * {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @throws NullPointerException                         if the given {@code OutputStream} is {@code null}.
     * @throws IOException                                  if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    public static void write(final char[] data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes all characters from a {@code char[]} to a {@link Writer}.
     *
     * @param data the char array to write, do not modify during output,
     * {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     */
    public static void write(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    /**
     * Writes all characters from a {@link CharSequence} to bytes on an
     * {@link OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * </p>
     *
     * @param data the {@code CharSequence} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.0
     * @deprecated 2.5 use {@link #write(CharSequence, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final CharSequence data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes all characters from a {@link CharSequence} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} for encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code CharSequence} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.3
     */
    public static void write(final CharSequence data, final OutputStream output, final Charset charset)
            throws IOException {
        if (data != null) {
            write(data.toString(), output, charset);
        }
    }

    /**
     * Writes all characters from a {@link CharSequence} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code CharSequence} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @throws NullPointerException        if the given {@code OutputStream} is {@code null}.
     * @throws IOException                 if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 2.0
     */
    public static void write(final CharSequence data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes all characters from a {@link CharSequence} to a {@link Writer}.
     *
     * @param data the {@code CharSequence} to write, {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.0
     */
    public static void write(final CharSequence data, final Writer writer) throws IOException {
        if (data != null) {
            write(data.toString(), writer);
        }
    }



    /**
     * Writes all characters from a {@link String} to bytes on an
     * {@link OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * </p>
     *
     * @param data the {@code String} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #write(String, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final String data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes all characters from a {@link String} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} for encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code String} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charset the {@code Charset} to use, {@code null} means platform default.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.3
     */
    public static void write(final String data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(data.getBytes(Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes all characters from a {@link String} to bytes on an
     * {@link OutputStream} using the specified {@link Charset} for encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code String} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@code Charset}, {@code null} means platform default.
     * @throws NullPointerException        if the given {@code OutputStream} is {@code null}.
     * @throws IOException                 if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    public static void write(final String data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes all characters from a {@link String} to a {@link Writer}.
     *
     * @param data the {@code String} to write, {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     */
    public static void write(final String data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    /**
     * Writes all characters from a {@link StringBuffer} to bytes on an
     * {@link OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * </p>
     *
     * @param data the {@code StringBuffer} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated Use {@link #write(CharSequence, OutputStream)}
     */
    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output) //NOSONAR
            throws IOException {
        write(data, output, (String) null);
    }

    /**
     * Writes all characters from a {@link StringBuffer} to bytes on an
     * {@link OutputStream} using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data the {@code StringBuffer} to write, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@link Charset}, {@code null} means platform default.
     * @throws NullPointerException        if the given {@code OutputStream} is {@code null}.
     * @throws IOException                 if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     * @deprecated Use {@link #write(CharSequence, OutputStream, String)}.
     */
    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output, final String charsetName) //NOSONAR
            throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charsets.toCharset(charsetName)));
        }
    }

    /**
     * Writes all characters from a {@link StringBuffer} to a {@link Writer}.
     *
     * @param data the {@code StringBuffer} to write, {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated Use {@link #write(CharSequence, Writer)}
     */
    @Deprecated
    public static void write(final StringBuffer data, final Writer writer) //NOSONAR
            throws IOException {
        if (data != null) {
            writer.write(data.toString());
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to an {@link OutputStream} using chunked writes.
     * This is intended for writing very large byte arrays which might otherwise cause excessive
     * memory usage if the native code has to allocate a copy.
     *
     * @param data the byte array to write, do not modify during output, {@code null} ignored.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.5
     */
    public static void writeChunked(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    /**
     * Writes all characters from a {@code char[]} to a {@link Writer} using chunked writes.
     * This is intended for writing very large byte arrays which might otherwise cause excessive
     * memory usage if the native code has to allocate a copy.
     *
     * @param data the char array to write, do not modify during output, {@code null} ignored.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.5
     */
    public static void writeChunked(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                writer.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@link OutputStream} line by line, using the default character
     * encoding of the platform and the specified line ending.
     *
     * @param lines the lines to write, {@code null} ignored and {@code null} entries produce blank lines.
     * @param lineEnding the line separator to use, {@code null} uses system default.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     * @deprecated 2.5 use {@link #writeLines(Collection, String, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output) throws IOException {
        writeLines(lines, lineEnding, output, Charset.defaultCharset());
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@link OutputStream} line by line, using the specified character
     * encoding and the specified line ending.
     *
     * @param lines the lines to write, {@code null} ignored and {@code null} entries produce blank lines.
     * @param lineEnding the line separator to use, {@code null} uses system default.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charset the {@link Charset} to use, null uses platform default.
     * @throws NullPointerException if the given {@code OutputStream} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 2.3
     */
    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output,
                                  final Charset charset) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        final Charset cs = Charsets.toCharset(charset);
        for (final Object line : lines) {
            if (line != null) {
                output.write(line.toString().getBytes(cs));
            }
            output.write(lineEnding.getBytes(cs));
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@link OutputStream} line by line, using the specified character
     * encoding and the specified line ending.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * </p>
     *
     * @param lines the lines to write, {@code null} ignored and {@code null} entries produce blank lines.
     * @param lineEnding the line separator to use, {@code null} is system default.
     * @param output the {@code OutputStream} to write to, not {@code null}, not closed.
     * @param charsetName the name of the requested {@link Charset}, null uses platform default.
     * @throws NullPointerException                         if the given {@code OutputStream} is {@code null}.
     * @throws IOException                                  if an I/O exception occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output, final String charsetName) throws IOException {
        writeLines(lines, lineEnding, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * a {@link Writer} line by line, using the specified line ending.
     *
     * @param lines the lines to write, {@code null} ignored and {@code null} entries produce blank lines.
     * @param lineEnding the line separator to use, {@code null} uses system default.
     * @param writer the {@code Writer} to write to, not {@code null}, not closed.
     * @throws NullPointerException if the given {@code Writer} is {@code null}.
     * @throws IOException          if an I/O exception occurs.
     * @since 1.1
     */
    public static void writeLines(final Collection<?> lines, String lineEnding,
                                  final Writer writer) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        for (final Object line : lines) {
            if (line != null) {
                writer.write(line.toString());
            }
            writer.write(lineEnding);
        }
    }

    /**
     * Returns the given {@link Appendable} if it is already a {@link Writer}, otherwise creates a
     * {@code Writer} wrapper around the given {@code Appendable}.
     *
     * @param appendable the {@code Appendable} to wrap or return, not {@code null}.
     * @return The given {@code Appendable} or a {@code Writer} wrapper around the given {@code Appendable}.
     * @throws NullPointerException if the given {@code Appendable} is {@code null}.
     * @since 2.7
     */
    public static Writer writer(final Appendable appendable) {
        Objects.requireNonNull(appendable, "appendable");
        if (appendable instanceof Writer) {
            return (Writer) appendable;
        }
        if (appendable instanceof StringBuilder) {
            return new StringBuilderWriter((StringBuilder) appendable);
        }
        return new AppendableWriter<>(appendable);
    }

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() { //NOSONAR
    }

}
