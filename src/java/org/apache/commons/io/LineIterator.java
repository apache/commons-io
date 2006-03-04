/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * An Iterator over the lines in a <code>Reader</code>.
 * <p>
 * This iterator must be closed after use to avoid a resource leak.
 * If you read every line, then the final {@link #hasNext()} method
 * will close the iterator. If you do not fully read the iterator
 * then you must call the {@link #close()} method.
 * <p>
 * However, since the iterator methods can throw exception, we recommend
 * always calling close in a finally block:
 * <pre>
 * LineIterator it = FileUtils.lineIterator(file, "UTF-8");
 * try {
 *   while (it.hasNext()) {
 *     String line = it.nextLine();
 *     /// do something with line
 *   }
 * } finally {
 *   LineIterator.closeQuietly(iterator);
 * }
 * </pre>
 *
 * @author Niall Pemberton
 * @author Stephen Colebourne
 * @version $Id$
 * @since Commons IO 1.2
 */
public class LineIterator implements IOIterator {

    /** The reader that is being read. */
    private final BufferedReader bufferedReader;
    /** The current line. */
    private String cachedLine;
    /** A flag indicating if the iterator has been fully read. */
    private boolean finished = false;

    /**
     * Constructs an iterator of the lines for a <code>Reader</code>.
     *
     * @param reader the <code>Reader</code> to read from, not null
     * @throws NullPointerException if the reader is null
     */
    public LineIterator(Reader reader) {
        if (reader == null) {
            throw new NullPointerException("Reader must not be null");
        }
        if (reader instanceof BufferedReader) {
            this.bufferedReader = (BufferedReader) reader;
        } else {
            this.bufferedReader = new BufferedReader(reader);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Indicates whether the <code>Reader</code> has more lines.
     *
     * @return <code>true</code> if the Reader has more lines
     * @throws IllegalStateException if an IO exception occurs
     */
    public boolean hasNext() {
        if (cachedLine != null) {
            return true;
        } else if (finished) {
            return false;
        } else {
            try {
                cachedLine = bufferedReader.readLine();
                if (cachedLine == null) {
                    close();
                    return false;
                } else {
                    return true;
                }
            } catch(IOException ioe) {
                close();
                throw new IllegalStateException(ioe.toString());
            }
        }
    }

    /**
     * Returns the next line in the wrapped <code>Reader</code>.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     */
    public Object next() {
        return nextLine();
    }

    /**
     * Returns the next line in the wrapped <code>Reader</code>.
     *
     * @return the next line from the input
     * @throws NoSuchElementException if there is no line to return
     */
    public String nextLine() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;        
    }

    /**
     * Closes the underlying <code>Reader</code> quietly.
     * This method is useful if you only want to process the first few
     * lines of a larger file. If you do not close the iterator
     * then the <code>Reader</code> remains open and is a resource leak.
     * This method can safely be called multiple times.
     */
    public void close() {
        if (!finished) {
            IOUtils.closeQuietly(bufferedReader);
            finished = true;
            cachedLine = null;
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException always
     */
    public void remove() {
        throw new UnsupportedOperationException("Remove unsupported on LineIterator");
    }

    /**
     * Finalize which closes the underlying reader.
     * Do not rely on this method to handle cleanup - call closeQuietly yourself.
     */
    protected void finalize() throws Throwable {
        close();
    }

    //-----------------------------------------------------------------------
    /**
     * Closes the iterator, handling null and ignoring exceptions.
     *
     * @param iterator  the iterator to close
     */
    public static void closeQuietly(LineIterator iterator) {
        if (iterator != null) {
            iterator.close();
        }
    }

}
