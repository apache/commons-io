/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/DemuxTestCase.java,v 1.8 2003/12/30 07:00:03 bayard Exp $
 * $Revision: 1.8 $
 * $Date: 2003/12/30 07:00:03 $
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
 *
 */

package org.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.DemuxOutputStream;
import org.apache.commons.io.input.DemuxInputStream;

/**
 * Basic unit tests for the multiplexing streams.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 */
public class DemuxTestCase
    extends TestCase
{
    private static String T1 = "Thread1";
    private static String T2 = "Thread2";
    private static String T3 = "Thread3";
    private static String T4 = "Thread4";

    private static String DATA1 = "Data for thread1";
    private static String DATA2 = "Data for thread2";
    private static String DATA3 = "Data for thread3";
    private static String DATA4 = "Data for thread4";

    private static Random c_random = new Random();
    private HashMap m_outputMap = new HashMap();
    private HashMap m_threadMap = new HashMap();

    public DemuxTestCase( String name )
    {
        super( name );
    }

    private String getOutput( String threadName )
        throws IOException
    {
        ByteArrayOutputStream output =
            (ByteArrayOutputStream)m_outputMap.get( threadName );
        assertNotNull( "getOutput()", output );

        return output.toString();
    }

    private String getInput( String threadName )
        throws IOException
    {
        ReaderThread thread = (ReaderThread)m_threadMap.get( threadName );
        assertNotNull( "getInput()", thread );

        return thread.getData();
    }

    private void doStart()
        throws Exception
    {
        Iterator iterator = m_threadMap.keySet().iterator();
        while( iterator.hasNext() )
        {
            String name = (String)iterator.next();
            Thread thread = (Thread)m_threadMap.get( name );
            thread.start();
        }
    }

    private void doJoin()
        throws Exception
    {
        Iterator iterator = m_threadMap.keySet().iterator();
        while( iterator.hasNext() )
        {
            String name = (String)iterator.next();
            Thread thread = (Thread)m_threadMap.get( name );
            thread.join();
        }
    }

    private void startWriter( String name,
                              String data,
                              DemuxOutputStream demux )
        throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        m_outputMap.put( name, output );
        WriterThread thread =
            new WriterThread( name, data, output, demux );
        m_threadMap.put( name, thread );
    }

    private void startReader( String name,
                              String data,
                              DemuxInputStream demux )
        throws Exception
    {
        ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes() );
        ReaderThread thread = new ReaderThread( name, input, demux );
        m_threadMap.put( name, thread );
    }

    public void testOutputStream()
        throws Exception
    {
        DemuxOutputStream output = new DemuxOutputStream();
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
        DemuxInputStream input = new DemuxInputStream();
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
        private StringBuffer m_buffer = new StringBuffer();
        private InputStream m_input;
        private DemuxInputStream m_demux;

        ReaderThread( String name,
                      InputStream input,
                      DemuxInputStream demux )
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

                    int sleepTime = Math.abs( c_random.nextInt() % 10 );
                    Thread.sleep( sleepTime );
                    ch = m_demux.read();
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private static class WriterThread
        extends Thread
    {
        private byte[] m_data;
        private OutputStream m_output;
        private DemuxOutputStream m_demux;

        WriterThread( String name,
                      String data,
                      OutputStream output,
                      DemuxOutputStream demux )
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
                    int sleepTime = Math.abs( c_random.nextInt() % 10 );
                    Thread.sleep( sleepTime );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

