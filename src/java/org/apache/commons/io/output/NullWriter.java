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
package org.apache.commons.io.output;

import java.io.Writer;

/**
 * This {@link Writer} writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This <code>Writer</code> has no destination (file/socket etc.) and all
 * characters written to it are ignored and lost.
 * 
 * @version $Id$
 */
public class NullWriter extends Writer {

    /**
     * Constructs a new NullWriter.
     */
    public NullWriter() {
    }

    /** @see java.io.Writer#write(int) */
    public void write(int idx) {
        //to /dev/null
    }

    /** @see java.io.Writer#write(char[]) */
    public void write(char[] chr) {
        //to /dev/null
    }

    /** @see java.io.Writer#write(char[], int, int) */
    public void write(char[] chr, int st, int end) {
        //to /dev/null
    }

    /** @see java.io.Writer#write(String) */
    public void write(String str) {
        //to /dev/null
    }

    /** @see java.io.Writer#write(String, int, int) */
    public void write(String str, int st, int end) {
        //to /dev/null
    }

    /** @see java.io.Writer#flush() */
    public void flush() {
        //to /dev/null
    }

    /** @see java.io.Writer#close() */
    public void close() {
        //to /dev/null
    }

}
