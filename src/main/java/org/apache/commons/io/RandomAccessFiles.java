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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Works on RandomAccessFile.
 *
 * @since 2.13.0
 */
public class RandomAccessFiles {

    /**
     * Reads a byte array starting at "position" for "length" bytes.
     *
     * @param input    The source RandomAccessFile.
     * @param position The offset position, measured in bytes from the beginning of the file, at which to set the file pointer.
     * @param length   How many bytes to read.
     * @return a new byte array.
     * @throws IOException If the first byte cannot be read for any reason other than end of file, or if the random access file has been closed, or if some
     *                     other I/O error occurs.
     */
    public static byte[] read(final RandomAccessFile input, final long position, final int length) throws IOException {
        input.seek(position);
        return IOUtils.toByteArray(input::read, length);
    }

}
