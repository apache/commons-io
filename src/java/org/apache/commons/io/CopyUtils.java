/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Utility methods for copying data.
 * 
 * @author Peter Donald
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @version $Id: CopyUtils.java,v 1.5 2004/02/23 04:35:59 bayard Exp $
 */
public class CopyUtils {

    /**
     * The name says it all.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public CopyUtils() {}

    // ----------------------------------------------------------------
    // byte[] -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Copy bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
     * @param input the byte array to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(byte[] input, OutputStream output)
            throws IOException {
        output.write(input);
    }

    // ----------------------------------------------------------------
    // byte[] -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the byte array to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(byte[] input, Writer output)
            throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output);
    }


    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     * @param input the byte array to read from
     * @param output the <code>Writer</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            byte[] input,
            Writer output,
            String encoding)
                throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output, encoding);
    }


    // ----------------------------------------------------------------
    // Core copy methods
    // ----------------------------------------------------------------

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(
            InputStream input,
            OutputStream output)
                throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    // ----------------------------------------------------------------
    // Reader -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
     * @param input the <code>Reader</code> to read from
     * @param output the <code>Writer</code> to write to
     * @return the number of characters copied
     * @throws IOException In case of an I/O problem
     */
    public static int copy(
            Reader input,
            Writer output)
                throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    // ----------------------------------------------------------------
    // InputStream -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            InputStream input,
            Writer output)
                throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>Writer</code> to write to
     * @param encoding The name of a supported character encoding. See the
     * <a href="http://www.iana.org/assignments/character-sets">IANA
     * Charset Registry</a> for a list of valid encoding types.
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            InputStream input,
            Writer output,
            String encoding)
                throws IOException {
        InputStreamReader in = new InputStreamReader(input, encoding);
        copy(in, output);
    }


    // ----------------------------------------------------------------
    // Reader -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Serialize chars from a <code>Reader</code> to bytes on an 
     * <code>OutputStream</code>, and flush the <code>OutputStream</code>.
     * @param input the <code>Reader</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            Reader input,
            OutputStream output)
                throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(output);
        copy(input, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we have to flush here.
        out.flush();
    }

    // ----------------------------------------------------------------
    // String -> OutputStream
    // ----------------------------------------------------------------

    /**
     * Serialize chars from a <code>String</code> to bytes on an <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     * @param input the <code>String</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(
            String input,
            OutputStream output)
                throws IOException {
        StringReader in = new StringReader(input);
        OutputStreamWriter out = new OutputStreamWriter(output);
        copy(in, out);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter, we have to flush here.
        out.flush();
    }

    // ----------------------------------------------------------------
    // String -> Writer
    // ----------------------------------------------------------------

    /**
     * Copy chars from a <code>String</code> to a <code>Writer</code>.
     * @param input the <code>String</code> to read from
     * @param output the <code>Writer</code> to write to
     * @throws IOException In case of an I/O problem
     */
    public static void copy(String input, Writer output)
                throws IOException {
        output.write(input);
    }

} // CopyUtils
