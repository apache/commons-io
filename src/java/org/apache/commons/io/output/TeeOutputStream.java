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
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Classic splitter of OutputStream. Named after the unix 'tee' 
 * command. It allows a stream to be branched off so there 
 * are now two streams.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @version $Id: TeeOutputStream.java,v 1.6 2004/02/23 04:53:04 bayard Exp $
 */
public class TeeOutputStream extends ProxyOutputStream {

    /** the second OutputStream to write to */
    protected OutputStream branch;

    /**
     * Constructs a TeeOutputStream.
     * @param out the main OutputStream
     * @param branch the second OutputStream
     */
    public TeeOutputStream( OutputStream out, OutputStream branch ) {
        super(out);
        this.branch = branch;
    }

    /** @see java.io.OutputStream#write(byte[]) */
    public synchronized void write(byte[] b) throws IOException {
        super.write(b);
        this.branch.write(b);
    }

    /** @see java.io.OutputStream#write(byte[], int, int) */
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        this.branch.write(b, off, len);
    }

    /** @see java.io.OutputStream#write(int) */
    public synchronized void write(int b) throws IOException {
        super.write(b);
        this.branch.write(b);
    }

    /**
     * Flushes both streams.
     *
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        super.flush();
        this.branch.flush();
    }

    /**
     * Closes both streams. 
     *
     * @see java.io.OutputStream#close() 
     */
    public void close() throws IOException {
        super.close();
        this.branch.close();
    }

}
