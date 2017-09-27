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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;

/**
 * Data written to this stream is forwarded to a stream that has been associated
 * with this thread.
 *
 */
public class DemuxInputStream
    extends InputStream
{
    private final InheritableThreadLocal<InputStream> m_streams = new InheritableThreadLocal<>();

    /**
     * Bind the specified stream to the current thread.
     *
     * @param input the stream to bind
     * @return the InputStream that was previously active
     */
    public InputStream bindStream( final InputStream input )
    {
        final InputStream oldValue = m_streams.get();
        m_streams.set( input );
        return oldValue;
    }

    /**
     * Closes stream associated with current thread.
     *
     * @throws IOException if an error occurs
     */
    @Override
    public void close()
        throws IOException
    {
        final InputStream input = m_streams.get();
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
    @Override
    public int read()
        throws IOException
    {
        final InputStream input = m_streams.get();
        if( null != input )
        {
            return input.read();
        }
        else
        {
            return EOF;
        }
    }
}
