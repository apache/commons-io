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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * General IO Stream manipulation.
 * <p>
 * This class provides static utility methods for input/output operations.
 * <p>
 * The closeQuietly methods are expected to be used when an IOException 
 * would be meaningless. This is usually when in a catch block for an 
 * IOException.
 * <p>
 * The toString and toByteArray methods all rely on CopyUtils.copy 
 * methods in the current implementation.
 *
 * <p>Origin of code: Apache Avalon (Excalibur)</p>
 *
 * @author Peter Donald
 * @author Jeff Turner
 * @author Stephen Colebourne
 * @version CVS $Revision: 1.17 $ $Date: 2004/07/31 09:52:09 $
 */
public class IOUtils {

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() {
    }

    //-----------------------------------------------------------------------
    /**
     * Unconditionally close an <code>Reader</code>.
     * <p>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param input  the Reader to close, may be null or already closed
     */
    public static void closeQuietly(Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close a <code>Writer</code>.
     * <p>
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param output  the Writer to close, may be null or already closed
     */
    public static void closeQuietly(Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * <p>
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param input  the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close an <code>OutputStream</code>.
     * <p>
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param input  the OutputStream to close, may be null or already closed
     */
    public static void closeQuietly( OutputStream output ) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    // toByteArray
    //-----------------------------------------------------------------------
    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * 
     * @param input  the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy(input, output);
        return output.toByteArray();
    }

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>
     * using the default character encoding of the platform.
     * 
     * @param input  the <code>Reader</code> to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy(input, output);
        return output.toByteArray();
    }

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * 
     * @param input  the <code>Reader</code> to read from
     * @param encoding  the encoding to use, null means platform default
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(Reader input, String encoding) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy(input, output, encoding);
        return output.toByteArray();
    }

    /**
     * Get the contents of a <code>String</code> as a <code>byte[]</code>
     * using the default character encoding of the platform.
     * <p>
     * This is the same as {@link String#getBytes()}.
     * 
     * @param input  the <code>String</code> to convert
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     *  (never happens, but can't remove due to backwards compatibility)
     */
    public static byte[] toByteArray(String input) throws IOException {
        return input.getBytes();
    }

    /**
     * Get the contents of a <code>String</code> as a <code>byte[]</code>
     * using the specified character encoding.
     * <p>
     * This is based on {@link String#getBytes(String)}.
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * 
     * @param input  the <code>String</code> to convert
     * @param encoding  the encoding to use, null means platform default
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws UnsupportedEncodingException if the named charset is not supported
     */
    public static byte[] toByteArray(String input, String encoding) throws UnsupportedEncodingException {
        if (encoding == null) {
            return input.getBytes();
        }
        return input.getBytes(encoding);
    }

    // toString
    //-----------------------------------------------------------------------
    /**
     * Get the contents of an <code>InputStream</code> as a String
     * using the default character encoding of the platform.
     * 
     * @param input  the <code>InputStream</code> to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        CopyUtils.copy(input, sw);
        return sw.toString();
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * 
     * @param input  the <code>InputStream</code> to read from
     * @param encoding  the encoding to use, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(InputStream input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        CopyUtils.copy(input, sw, encoding);
        return sw.toString();
    }

    /**
     * Get the contents of a <code>Reader</code> as a String.
     * 
     * @param input  the <code>Reader</code> to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        CopyUtils.copy(input, sw);
        return sw.toString();
    }

    /**
     * Get the contents of a <code>byte[]</code> as a String
     * using the default character encoding of the platform.
     * 
     * @param input the byte array to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(byte[] input) throws IOException {
        StringWriter sw = new StringWriter();
        CopyUtils.copy(input, sw);
        return sw.toString();
    }

    /**
     * Get the contents of a <code>byte[]</code> as a String
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * 
     * @param input the byte array to read from
     * @param encoding  the encoding to use, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String toString(byte[] input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        CopyUtils.copy(input, sw, encoding);
        return sw.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Compare the contents of two Streams to determine if they are equal or not.
     *
     * @param input1  the first stream
     * @param input2  the second stream
     * @return true if the content of the streams are equal or they both don't exist, false otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException if an I/O error occurs
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        InputStream bufferedInput1 = new BufferedInputStream(input1);
        InputStream bufferedInput2 = new BufferedInputStream(input2);

        int ch = bufferedInput1.read();
        while (-1 != ch) {
            int ch2 = bufferedInput2.read();
            if (ch != ch2) {
                return false;
            }
            ch = bufferedInput1.read();
        }

        int ch2 = bufferedInput2.read();
        if (-1 != ch2) {
            return false;
        } else {
            return true;
        }
    }

}
