/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * This OutputStream writes all data to the famous <b>/dev/null</b>.
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: NullOutputStream.java,v 1.4 2004/02/23 04:40:29 bayard Exp $
 */
public class NullOutputStream extends OutputStream {

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public synchronized void write(byte[] b, int off, int len) {
        //to /dev/null
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public synchronized void write(int b) {
        //to /dev/null
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        //to /dev/null
    }

}
