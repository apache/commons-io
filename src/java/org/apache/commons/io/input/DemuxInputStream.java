/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

/**
 * Data written to this stream is forwarded to a stream that has been associated
 * with this thread.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.3 $ $Date: 2003/10/13 07:04:16 $
 */
public final class DemuxInputStream
    extends InputStream
{
    private final InheritableThreadLocal m_streams = new InheritableThreadLocal();

    /**
     * Bind the specified stream to the current thread.
     *
     * @param input the stream to bind
     * @return the InputStream that was previously active
     */
    public InputStream bindStream( final InputStream input )
    {
        final InputStream oldValue = getStream();
        m_streams.set( input );
        return oldValue;
    }

    /**
     * Closes stream associated with current thread.
     *
     * @throws IOException if an error occurs
     */
    public void close()
        throws IOException
    {
        final InputStream input = getStream();
        if( null != input )
        {
            input.close();
        }
    }

    /**
     * Read byte from stream associated with current thread.
     *
     * @return the byte read from stream
     * @throws IOException if an error occurs
     */
    public int read()
        throws IOException
    {
        final InputStream input = getStream();
        if( null != input )
        {
            return input.read();
        }
        else
        {
            return -1;
        }
    }

    /**
     * Utility method to retrieve stream bound to current thread (if any).
     */
    private InputStream getStream()
    {
        return (InputStream)m_streams.get();
    }
}
