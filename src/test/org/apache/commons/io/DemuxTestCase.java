/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/DemuxTestCase.java,v 1.2 2002/07/09 15:12:23 nicolaken Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/09 15:12:23 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 *
 */

package org.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import junit.framework.TestCase;

/**
 * Basic unit tests for the multiplexing streams.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 */
public final class DemuxTestCase
    extends TestCase
{
    private static final String T1 = "Thread1";
    private static final String T2 = "Thread2";
    private static final String T3 = "Thread3";
    private static final String T4 = "Thread4";

    private static final String DATA1 = "Data for thread1";
    private static final String DATA2 = "Data for thread2";
    private static final String DATA3 = "Data for thread3";
    private static final String DATA4 = "Data for thread4";

    private static final Random c_random = new Random();
    private final HashMap m_outputMap = new HashMap();
    private final HashMap m_threadMap = new HashMap();

    public DemuxTestCase( final String name )
    {
        super( name );
    }

    private String getOutput( final String threadName )
        throws IOException
    {
        final ByteArrayOutputStream output =
            (ByteArrayOutputStream)m_outputMap.get( threadName );
        assertNotNull( "getOutput()", output );

        return output.toString();
    }

    private String getInput( final String threadName )
        throws IOException
    {
        final ReaderThread thread = (ReaderThread)m_threadMap.get( threadName );
        assertNotNull( "getInput()", thread );

        return thread.getData();
    }

    private void doStart()
        throws Exception
    {
        final Iterator iterator = m_threadMap.keySet().iterator();
        while( iterator.hasNext() )
        {
            final String name = (String)iterator.next();
            final Thread thread = (Thread)m_threadMap.get( name );
            thread.start();
        }
    }

    private void doJoin()
        throws Exception
    {
        final Iterator iterator = m_threadMap.keySet().iterator();
        while( iterator.hasNext() )
        {
            final String name = (String)iterator.next();
            final Thread thread = (Thread)m_threadMap.get( name );
            thread.join();
        }
    }

    private void startWriter( final String name,
                              final String data,
                              final DemuxOutputStream demux )
        throws Exception
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        m_outputMap.put( name, output );
        final WriterThread thread =
            new WriterThread( name, data, output, demux );
        m_threadMap.put( name, thread );
    }

    private void startReader( final String name,
                              final String data,
                              final DemuxInputStream demux )
        throws Exception
    {
        final ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes() );
        final ReaderThread thread = new ReaderThread( name, input, demux );
        m_threadMap.put( name, thread );
    }

    public void testOutputStream()
        throws Exception
    {
        final DemuxOutputStream output = new DemuxOutputStream();
        startWriter( T1, DATA1, output );
        startWriter( T2, DATA2, output );
        startWriter( T3, DATA3, output );
        startWriter( T4, DATA4, output );

        doStart();
        doJoin();

        assertEquals( "Data1", DATA1, getOutput( T1 ) );
        assertEquals( "Data2", DATA2, getOutput( T2 ) );
        assertEquals( "Data3", DATA3, getOutput( T3 ) );
        assertEquals( "Data4", DATA4, getOutput( T4 ) );
    }

    public void testInputStream()
        throws Exception
    {
        final DemuxInputStream input = new DemuxInputStream();
        startReader( T1, DATA1, input );
        startReader( T2, DATA2, input );
        startReader( T3, DATA3, input );
        startReader( T4, DATA4, input );

        doStart();
        doJoin();

        assertEquals( "Data1", DATA1, getInput( T1 ) );
        assertEquals( "Data2", DATA2, getInput( T2 ) );
        assertEquals( "Data3", DATA3, getInput( T3 ) );
        assertEquals( "Data4", DATA4, getInput( T4 ) );
    }

    private static class ReaderThread
        extends Thread
    {
        private final StringBuffer m_buffer = new StringBuffer();
        private final InputStream m_input;
        private final DemuxInputStream m_demux;

        ReaderThread( final String name,
                      final InputStream input,
                      final DemuxInputStream demux )
        {
            super( name );
            m_input = input;
            m_demux = demux;
        }

        public String getData()
        {
            return m_buffer.toString();
        }

        public void run()
        {
            m_demux.bindStream( m_input );

            try
            {
                int ch = m_demux.read();
                while( -1 != ch )
                {
                    //System.out.println( "Reading: " + (char)ch );
                    m_buffer.append( (char)ch );

                    final int sleepTime = Math.abs( c_random.nextInt() % 10 );
                    Thread.sleep( sleepTime );
                    ch = m_demux.read();
                }
            }
            catch( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private static class WriterThread
        extends Thread
    {
        private final byte[] m_data;
        private final OutputStream m_output;
        private final DemuxOutputStream m_demux;

        WriterThread( final String name,
                      final String data,
                      final OutputStream output,
                      final DemuxOutputStream demux )
        {
            super( name );
            m_output = output;
            m_demux = demux;
            m_data = data.getBytes();
        }

        public void run()
        {
            m_demux.bindStream( m_output );
            for( int i = 0; i < m_data.length; i++ )
            {
                try
                {
                    //System.out.println( "Writing: " + (char)m_data[ i ] );
                    m_demux.write( m_data[ i ] );
                    final int sleepTime = Math.abs( c_random.nextInt() % 10 );
                    Thread.sleep( sleepTime );
                }
                catch( final Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

