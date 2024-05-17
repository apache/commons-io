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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An Iterator over the lines in a {@link Reader}.
 * <p>
 * {@link LineIterator} holds a reference to an open {@link Reader}.
 * When you have finished with the iterator you should close the reader
 * to free internal resources. This can be done by closing the reader directly,
 * or by calling the {@link #close()} or {@link #closeQuietly(LineIterator)}
 * method on the iterator.
 * <p>
 * The recommended usage pattern is:
 * <pre>
 * LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name());
 * try {
 *   while (it.hasNext()) {
 *     String line = it.nextLine();
 *     // do something with line
 *   }
 * } finally {
 *   it.close();
 * }
 * </pre>
 *
 * @since 1.2
 */
public class LineIterator implements Iterator<String>, Closeable {

    // N.B. This class deliberately does not implement Iterable, see https://issues.apache.org/jira/browse/IO-181

    /**
     * Closes a {@link LineIterator} quietly.
     *
     * @param iterator The iterator to close, or {@code null}.
     * @deprecated As of 2.6 deprecated without replacement. Please use the try-with-resources statement or handle
     * suppressed exceptions manually.
     * @see Throwable#addSuppressed(Throwable)
     */
    @Deprecated
    public static void closeQuietly(final LineIterator iterator) {
        IOUtils.closeQuietly(iterator);
    }

    /** The reader that is being read. */
    private final BufferedReader bufferedReader;

    /** The current line. */
    private String cachedLine;

    /** A flag indicating if the iterator has been fully read. */
    private boolean finished;

    /**
     * Constructs an iterator of the lines for a {@link Reader}.
     *
     * @param reader the {@link Reader} to read from, not null
     * @throws NullPointerException if the reader is null
     */
    @SuppressWarnings("resource") // Caller closes Reader
    public LineIterator(final Reader reader) {
        Objects.requireNonNull(reader, "reader");
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
    }

    /**
     * Closes the underlying {@link Reader}.
     * This method is useful if you only want to process the first few
     * lines of a larger file. If you do not close the iterator
     * then the {@link Reader} remains open.
     * This method can safely be called multiple times.
     *
     * @throws IOException if closing the underlying {@link Reader} fails.
     */
    @Override
    public void close() throws IOException {
        finished = true;
        cachedLine = null;
        IOUtils.close(bufferedReader);
    }

    /**
     * Indicates whether the {@link Reader} has more lines.
     * If there is an {@link IOException} then {@link #close()} will
     * be called on this instance.
     *
     * @return {@code true} if the Reader has more lines
     * @throws IllegalStateException if an IO exception occurs
     */
    @Override
    public boolean hasNext() {
        if (cachedLine != null) {
            return true;
        }
        if (finished) {
            return false;
        }
        try {
            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    finished = true;
                    return false;
                }
                if (isValidLine(line)) {
                    cachedLine = line;
                    return true;
                }
            }
        } catch (final IOException ioe) {
            IOUtils.closeQuietly(this, ioe::addSuppressed);
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Overridable method to validate each line that is returned.
     * This implementation always returns true.
     * @param line  the line that is to be validated
     * @return true if valid, false to remove from the iterator
     */
    protected boolean isValidLine(final String line) {
        return true;
    }

    /**
     * Returns the next line in the wrapped {@link Reader}.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     */
    @Override
    public String next() {
        return nextLine();
    }

    /**
     * Returns the next line in the wrapped {@link Reader}.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     * @deprecated Use {@link #next()}.
     */
    @Deprecated
    public String nextLine() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        final String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

}
