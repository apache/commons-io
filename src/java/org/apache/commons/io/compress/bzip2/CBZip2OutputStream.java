/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/bzip2/Attic/CBZip2OutputStream.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/13 22:37:46 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.commons.io.compress.bzip2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that compresses into the BZip2 format (without the file
 * header chars) into another stream. TODO: Update to BZip2 1.0.1
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class CBZip2OutputStream
    extends OutputStream
    implements BZip2Constants
{
    private static final int LOWER_BYTE_MASK = 0x000000ff;
    private static final int UPPER_BYTE_MASK = 0xffffff00;
    private static final int SETMASK = ( 1 << 21 );
    private static final int CLEARMASK = ( ~SETMASK );
    private static final int GREATER_ICOST = 15;
    private static final int LESSER_ICOST = 0;
    private static final int SMALL_THRESH = 20;
    private static final int DEPTH_THRESH = 10;

    /*
     * If you are ever unlucky/improbable enough
     * to get a stack overflow whilst sorting,
     * increase the following constant and try
     * again.  In practice I have never seen the
     * stack go above 27 elems, so the following
     * limit seems very generous.
     */
    private static final int QSORT_STACK_SIZE = 1000;

    private CRC m_crc = new CRC();

    private boolean[] m_inUse = new boolean[ 256 ];

    private char[] m_seqToUnseq = new char[ 256 ];
    private char[] m_unseqToSeq = new char[ 256 ];

    private char[] m_selector = new char[ MAX_SELECTORS ];
    private char[] m_selectorMtf = new char[ MAX_SELECTORS ];

    private int[] m_mtfFreq = new int[ MAX_ALPHA_SIZE ];

    private int m_currentChar = -1;
    private int m_runLength;

    private boolean m_closed;

    /*
     * Knuth's increments seem to work better
     * than Incerpi-Sedgewick here.  Possibly
     * because the number of elems to sort is
     * usually small, typically <= 20.
     */
    private int[] m_incs = new int[]
    {
        1, 4, 13, 40, 121, 364, 1093, 3280,
        9841, 29524, 88573, 265720,
        797161, 2391484
    };

    private boolean m_blockRandomised;

    /*
     * always: in the range 0 .. 9.
     * The current block size is 100000 * this number.
     */
    private int m_blockSize100k;
    private int m_bsBuff;
    private int m_bsLive;

    /*
     * index of the last char in the block, so
     * the block size == last + 1.
     */
    private int m_last;

    /*
     * index in zptr[] of original string after sorting.
     */
    private int m_origPtr;

    private int m_allowableBlockSize;

    private char[] m_block;

    private int m_blockCRC;
    private int m_combinedCRC;

    private OutputStream m_bsStream;
    private boolean m_firstAttempt;
    private int[] m_ftab;
    private int m_nInUse;

    private int m_nMTF;
    private int[] m_quadrant;
    private short[] m_szptr;
    private int m_workDone;

    /*
     * Used when sorting.  If too many long comparisons
     * happen, we stop sorting, randomise the block
     * slightly, and try again.
     */
    private int m_workFactor;
    private int m_workLimit;
    private int[] m_zptr;

    public CBZip2OutputStream( final OutputStream output )
        throws IOException
    {
        this( output, 9 );
    }

    public CBZip2OutputStream( final OutputStream output, final int blockSize )
        throws IOException
    {
        bsSetStream( output );
        m_workFactor = 50;

        int outBlockSize = blockSize;
        if( outBlockSize > 9 )
        {
            outBlockSize = 9;
        }
        if( outBlockSize < 1 )
        {
            outBlockSize = 1;
        }
        m_blockSize100k = outBlockSize;
        allocateCompressStructures();
        initialize();
        initBlock();
    }

    private static void hbMakeCodeLengths( char[] len, int[] freq,
                                           int alphaSize, int maxLen )
    {
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int nNodes;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int nHeap;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int n1;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int n2;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int i;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int j;
        /*
         * Nodes and heap entries run from 1.  Entry 0
         * for both the heap and nodes is a sentinel.
         */
        int k;
        boolean tooLong;

        int[] heap = new int[ MAX_ALPHA_SIZE + 2 ];
        int[] weights = new int[ MAX_ALPHA_SIZE * 2 ];
        int[] parent = new int[ MAX_ALPHA_SIZE * 2 ];

        for( i = 0; i < alphaSize; i++ )
        {
            weights[ i + 1 ] = ( freq[ i ] == 0 ? 1 : freq[ i ] ) << 8;
        }

        while( true )
        {
            nNodes = alphaSize;
            nHeap = 0;

            heap[ 0 ] = 0;
            weights[ 0 ] = 0;
            parent[ 0 ] = -2;

            for( i = 1; i <= alphaSize; i++ )
            {
                parent[ i ] = -1;
                nHeap++;
                heap[ nHeap ] = i;
                {
                    int zz;
                    int tmp;
                    zz = nHeap;
                    tmp = heap[ zz ];
                    while( weights[ tmp ] < weights[ heap[ zz >> 1 ] ] )
                    {
                        heap[ zz ] = heap[ zz >> 1 ];
                        zz >>= 1;
                    }
                    heap[ zz ] = tmp;
                }
            }
            if( !( nHeap < ( MAX_ALPHA_SIZE + 2 ) ) )
            {
                panic();
            }

            while( nHeap > 1 )
            {
                n1 = heap[ 1 ];
                heap[ 1 ] = heap[ nHeap ];
                nHeap--;
                {
                    int zz = 0;
                    int yy = 0;
                    int tmp = 0;
                    zz = 1;
                    tmp = heap[ zz ];
                    while( true )
                    {
                        yy = zz << 1;
                        if( yy > nHeap )
                        {
                            break;
                        }
                        if( yy < nHeap &&
                            weights[ heap[ yy + 1 ] ] < weights[ heap[ yy ] ] )
                        {
                            yy++;
                        }
                        if( weights[ tmp ] < weights[ heap[ yy ] ] )
                        {
                            break;
                        }
                        heap[ zz ] = heap[ yy ];
                        zz = yy;
                    }
                    heap[ zz ] = tmp;
                }
                n2 = heap[ 1 ];
                heap[ 1 ] = heap[ nHeap ];
                nHeap--;
                {
                    int zz = 0;
                    int yy = 0;
                    int tmp = 0;
                    zz = 1;
                    tmp = heap[ zz ];
                    while( true )
                    {
                        yy = zz << 1;
                        if( yy > nHeap )
                        {
                            break;
                        }
                        if( yy < nHeap &&
                            weights[ heap[ yy + 1 ] ] < weights[ heap[ yy ] ] )
                        {
                            yy++;
                        }
                        if( weights[ tmp ] < weights[ heap[ yy ] ] )
                        {
                            break;
                        }
                        heap[ zz ] = heap[ yy ];
                        zz = yy;
                    }
                    heap[ zz ] = tmp;
                }
                nNodes++;
                parent[ n1 ] = nNodes;
                parent[ n2 ] = nNodes;

                final int v1 = weights[ n1 ];
                final int v2 = weights[ n2 ];
                final int weight = calculateWeight( v1, v2 );
                weights[ nNodes ] = weight;

                parent[ nNodes ] = -1;
                nHeap++;
                heap[ nHeap ] = nNodes;
                {
                    int zz = 0;
                    int tmp = 0;
                    zz = nHeap;
                    tmp = heap[ zz ];
                    while( weights[ tmp ] < weights[ heap[ zz >> 1 ] ] )
                    {
                        heap[ zz ] = heap[ zz >> 1 ];
                        zz >>= 1;
                    }
                    heap[ zz ] = tmp;
                }
            }
            if( !( nNodes < ( MAX_ALPHA_SIZE * 2 ) ) )
            {
                panic();
            }

            tooLong = false;
            for( i = 1; i <= alphaSize; i++ )
            {
                j = 0;
                k = i;
                while( parent[ k ] >= 0 )
                {
                    k = parent[ k ];
                    j++;
                }
                len[ i - 1 ] = (char)j;
                if( j > maxLen )
                {
                    tooLong = true;
                }
            }

            if( !tooLong )
            {
                break;
            }

            for( i = 1; i < alphaSize; i++ )
            {
                j = weights[ i ] >> 8;
                j = 1 + ( j / 2 );
                weights[ i ] = j << 8;
            }
        }
    }

    private static int calculateWeight( final int v1, final int v2 )
    {
        final int upper = ( v1 & UPPER_BYTE_MASK ) + ( v2 & UPPER_BYTE_MASK );
        final int v1Lower = ( v1 & LOWER_BYTE_MASK );
        final int v2Lower = ( v2 & LOWER_BYTE_MASK );
        final int nnnn = ( v1Lower > v2Lower ) ? v1Lower : v2Lower;
        return upper | ( 1 + nnnn );
    }

    private static void panic()
    {
        System.out.println( "panic" );
        //throw new CError();
    }

    public void close()
        throws IOException
    {
        if( m_closed )
        {
            return;
        }

        if( m_runLength > 0 )
        {
            writeRun();
        }
        m_currentChar = -1;
        endBlock();
        endCompression();
        m_closed = true;
        super.close();
        m_bsStream.close();
    }

    public void finalize()
        throws Throwable
    {
        close();
    }

    public void flush()
        throws IOException
    {
        super.flush();
        m_bsStream.flush();
    }

    /**
     * modified by Oliver Merkel, 010128
     *
     * @param bv Description of Parameter
     * @exception java.io.IOException Description of Exception
     */
    public void write( int bv )
        throws IOException
    {
        int b = ( 256 + bv ) % 256;
        if( m_currentChar != -1 )
        {
            if( m_currentChar == b )
            {
                m_runLength++;
                if( m_runLength > 254 )
                {
                    writeRun();
                    m_currentChar = -1;
                    m_runLength = 0;
                }
            }
            else
            {
                writeRun();
                m_runLength = 1;
                m_currentChar = b;
            }
        }
        else
        {
            m_currentChar = b;
            m_runLength++;
        }
    }

    private void allocateCompressStructures()
    {
        int n = BASE_BLOCK_SIZE * m_blockSize100k;
        m_block = new char[ ( n + 1 + NUM_OVERSHOOT_BYTES ) ];
        m_quadrant = new int[ ( n + NUM_OVERSHOOT_BYTES ) ];
        m_zptr = new int[ n ];
        m_ftab = new int[ 65537 ];

        if( m_block == null || m_quadrant == null || m_zptr == null
            || m_ftab == null )
        {
            //int totalDraw = (n + 1 + NUM_OVERSHOOT_BYTES) + (n + NUM_OVERSHOOT_BYTES) + n + 65537;
            //compressOutOfMemory ( totalDraw, n );
        }

        /*
         * The back end needs a place to store the MTF values
         * whilst it calculates the coding tables.  We could
         * put them in the zptr array.  However, these values
         * will fit in a short, so we overlay szptr at the
         * start of zptr, in the hope of reducing the number
         * of cache misses induced by the multiple traversals
         * of the MTF values when calculating coding tables.
         * Seems to improve compression speed by about 1%.
         */
        //    szptr = zptr;

        m_szptr = new short[ 2 * n ];
    }

    private void bsFinishedWithStream()
        throws IOException
    {
        while( m_bsLive > 0 )
        {
            int ch = ( m_bsBuff >> 24 );
            try
            {
                m_bsStream.write( ch );// write 8-bit
            }
            catch( IOException e )
            {
                throw e;
            }
            m_bsBuff <<= 8;
            m_bsLive -= 8;
        }
    }

    private void bsPutIntVS( int numBits, int c )
        throws IOException
    {
        bsW( numBits, c );
    }

    private void bsPutUChar( int c )
        throws IOException
    {
        bsW( 8, c );
    }

    private void bsPutint( int u )
        throws IOException
    {
        bsW( 8, ( u >> 24 ) & 0xff );
        bsW( 8, ( u >> 16 ) & 0xff );
        bsW( 8, ( u >> 8 ) & 0xff );
        bsW( 8, u & 0xff );
    }

    private void bsSetStream( OutputStream f )
    {
        m_bsStream = f;
        m_bsLive = 0;
        m_bsBuff = 0;
    }

    private void bsW( int n, int v )
        throws IOException
    {
        while( m_bsLive >= 8 )
        {
            int ch = ( m_bsBuff >> 24 );
            try
            {
                m_bsStream.write( ch );// write 8-bit
            }
            catch( IOException e )
            {
                throw e;
            }
            m_bsBuff <<= 8;
            m_bsLive -= 8;
        }
        m_bsBuff |= ( v << ( 32 - m_bsLive - n ) );
        m_bsLive += n;
    }

    private void doReversibleTransformation()
    {
        int i;

        m_workLimit = m_workFactor * m_last;
        m_workDone = 0;
        m_blockRandomised = false;
        m_firstAttempt = true;

        mainSort();

        if( m_workDone > m_workLimit && m_firstAttempt )
        {
            randomiseBlock();
            m_workLimit = 0;
            m_workDone = 0;
            m_blockRandomised = true;
            m_firstAttempt = false;
            mainSort();
        }

        m_origPtr = -1;
        for( i = 0; i <= m_last; i++ )
        {
            if( m_zptr[ i ] == 0 )
            {
                m_origPtr = i;
                break;
            }
        }
        ;

        if( m_origPtr == -1 )
        {
            panic();
        }
    }

    private void endBlock()
        throws IOException
    {
        m_blockCRC = m_crc.getFinalCRC();
        m_combinedCRC = ( m_combinedCRC << 1 ) | ( m_combinedCRC >>> 31 );
        m_combinedCRC ^= m_blockCRC;

        /*
         * sort the block and establish posn of original string
         */
        doReversibleTransformation();

        /*
         * A 6-byte block header, the value chosen arbitrarily
         * as 0x314159265359 :-).  A 32 bit value does not really
         * give a strong enough guarantee that the value will not
         * appear by chance in the compressed datastream.  Worst-case
         * probability of this event, for a 900k block, is about
         * 2.0e-3 for 32 bits, 1.0e-5 for 40 bits and 4.0e-8 for 48 bits.
         * For a compressed file of size 100Gb -- about 100000 blocks --
         * only a 48-bit marker will do.  NB: normal compression/
         * decompression do *not* rely on these statistical properties.
         * They are only important when trying to recover blocks from
         * damaged files.
         */
        bsPutUChar( 0x31 );
        bsPutUChar( 0x41 );
        bsPutUChar( 0x59 );
        bsPutUChar( 0x26 );
        bsPutUChar( 0x53 );
        bsPutUChar( 0x59 );

        /*
         * Now the block's CRC, so it is in a known place.
         */
        bsPutint( m_blockCRC );

        /*
         * Now a single bit indicating randomisation.
         */
        if( m_blockRandomised )
        {
            bsW( 1, 1 );
        }
        else
        {
            bsW( 1, 0 );
        }

        /*
         * Finally, block's contents proper.
         */
        moveToFrontCodeAndSend();
    }

    private void endCompression()
        throws IOException
    {
        /*
         * Now another magic 48-bit number, 0x177245385090, to
         * indicate the end of the last block.  (sqrt(pi), if
         * you want to know.  I did want to use e, but it contains
         * too much repetition -- 27 18 28 18 28 46 -- for me
         * to feel statistically comfortable.  Call me paranoid.)
         */
        bsPutUChar( 0x17 );
        bsPutUChar( 0x72 );
        bsPutUChar( 0x45 );
        bsPutUChar( 0x38 );
        bsPutUChar( 0x50 );
        bsPutUChar( 0x90 );

        bsPutint( m_combinedCRC );

        bsFinishedWithStream();
    }

    private boolean fullGtU( int i1, int i2 )
    {
        int k;
        char c1;
        char c2;
        int s1;
        int s2;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        c1 = m_block[ i1 + 1 ];
        c2 = m_block[ i2 + 1 ];
        if( c1 != c2 )
        {
            return ( c1 > c2 );
        }
        i1++;
        i2++;

        k = m_last + 1;

        do
        {
            c1 = m_block[ i1 + 1 ];
            c2 = m_block[ i2 + 1 ];
            if( c1 != c2 )
            {
                return ( c1 > c2 );
            }
            s1 = m_quadrant[ i1 ];
            s2 = m_quadrant[ i2 ];
            if( s1 != s2 )
            {
                return ( s1 > s2 );
            }
            i1++;
            i2++;

            c1 = m_block[ i1 + 1 ];
            c2 = m_block[ i2 + 1 ];
            if( c1 != c2 )
            {
                return ( c1 > c2 );
            }
            s1 = m_quadrant[ i1 ];
            s2 = m_quadrant[ i2 ];
            if( s1 != s2 )
            {
                return ( s1 > s2 );
            }
            i1++;
            i2++;

            c1 = m_block[ i1 + 1 ];
            c2 = m_block[ i2 + 1 ];
            if( c1 != c2 )
            {
                return ( c1 > c2 );
            }
            s1 = m_quadrant[ i1 ];
            s2 = m_quadrant[ i2 ];
            if( s1 != s2 )
            {
                return ( s1 > s2 );
            }
            i1++;
            i2++;

            c1 = m_block[ i1 + 1 ];
            c2 = m_block[ i2 + 1 ];
            if( c1 != c2 )
            {
                return ( c1 > c2 );
            }
            s1 = m_quadrant[ i1 ];
            s2 = m_quadrant[ i2 ];
            if( s1 != s2 )
            {
                return ( s1 > s2 );
            }
            i1++;
            i2++;

            if( i1 > m_last )
            {
                i1 -= m_last;
                i1--;
            }
            ;
            if( i2 > m_last )
            {
                i2 -= m_last;
                i2--;
            }
            ;

            k -= 4;
            m_workDone++;
        } while( k >= 0 );

        return false;
    }

    private void generateMTFValues()
    {
        char[] yy = new char[ 256 ];
        int i;
        int j;
        char tmp;
        char tmp2;
        int zPend;
        int wr;
        int EOB;

        makeMaps();
        EOB = m_nInUse + 1;

        for( i = 0; i <= EOB; i++ )
        {
            m_mtfFreq[ i ] = 0;
        }

        wr = 0;
        zPend = 0;
        for( i = 0; i < m_nInUse; i++ )
        {
            yy[ i ] = (char)i;
        }

        for( i = 0; i <= m_last; i++ )
        {
            char ll_i;

            ll_i = m_unseqToSeq[ m_block[ m_zptr[ i ] ] ];

            j = 0;
            tmp = yy[ j ];
            while( ll_i != tmp )
            {
                j++;
                tmp2 = tmp;
                tmp = yy[ j ];
                yy[ j ] = tmp2;
            }
            ;
            yy[ 0 ] = tmp;

            if( j == 0 )
            {
                zPend++;
            }
            else
            {
                if( zPend > 0 )
                {
                    zPend--;
                    while( true )
                    {
                        switch( zPend % 2 )
                        {
                            case 0:
                                m_szptr[ wr ] = (short)RUNA;
                                wr++;
                                m_mtfFreq[ RUNA ]++;
                                break;
                            case 1:
                                m_szptr[ wr ] = (short)RUNB;
                                wr++;
                                m_mtfFreq[ RUNB ]++;
                                break;
                        }
                        ;
                        if( zPend < 2 )
                        {
                            break;
                        }
                        zPend = ( zPend - 2 ) / 2;
                    }
                    ;
                    zPend = 0;
                }
                m_szptr[ wr ] = (short)( j + 1 );
                wr++;
                m_mtfFreq[ j + 1 ]++;
            }
        }

        if( zPend > 0 )
        {
            zPend--;
            while( true )
            {
                switch( zPend % 2 )
                {
                    case 0:
                        m_szptr[ wr ] = (short)RUNA;
                        wr++;
                        m_mtfFreq[ RUNA ]++;
                        break;
                    case 1:
                        m_szptr[ wr ] = (short)RUNB;
                        wr++;
                        m_mtfFreq[ RUNB ]++;
                        break;
                }
                if( zPend < 2 )
                {
                    break;
                }
                zPend = ( zPend - 2 ) / 2;
            }
        }

        m_szptr[ wr ] = (short)EOB;
        wr++;
        m_mtfFreq[ EOB ]++;

        m_nMTF = wr;
    }

    private void hbAssignCodes( int[] code, char[] length, int minLen,
                                int maxLen, int alphaSize )
    {
        int n;
        int vec;
        int i;

        vec = 0;
        for( n = minLen; n <= maxLen; n++ )
        {
            for( i = 0; i < alphaSize; i++ )
            {
                if( length[ i ] == n )
                {
                    code[ i ] = vec;
                    vec++;
                }
            }
            ;
            vec <<= 1;
        }
    }

    private void initBlock()
    {
        //        blockNo++;
        m_crc.initialiseCRC();
        m_last = -1;
        //        ch = 0;

        for( int i = 0; i < 256; i++ )
        {
            m_inUse[ i ] = false;
        }

        /*
         * 20 is just a paranoia constant
         */
        m_allowableBlockSize = BASE_BLOCK_SIZE * m_blockSize100k - 20;
    }

    private void initialize()
        throws IOException
    {
        /*
         * Write `magic' bytes h indicating file-format == huffmanised,
         * followed by a digit indicating blockSize100k.
         */
        bsPutUChar( 'h' );
        bsPutUChar( '0' + m_blockSize100k );

        m_combinedCRC = 0;
    }

    private void mainSort()
    {
        int i;
        int j;
        int ss;
        int sb;
        int[] runningOrder = new int[ 256 ];
        int[] copy = new int[ 256 ];
        boolean[] bigDone = new boolean[ 256 ];
        int c1;
        int c2;

        /*
         * In the various block-sized structures, live data runs
         * from 0 to last+NUM_OVERSHOOT_BYTES inclusive.  First,
         * set up the overshoot area for block.
         */
        //   if (verbosity >= 4) fprintf ( stderr, "        sort initialise ...\n" );
        for( i = 0; i < NUM_OVERSHOOT_BYTES; i++ )
        {
            m_block[ m_last + i + 2 ] = m_block[ ( i % ( m_last + 1 ) ) + 1 ];
        }
        for( i = 0; i <= m_last + NUM_OVERSHOOT_BYTES; i++ )
        {
            m_quadrant[ i ] = 0;
        }

        m_block[ 0 ] = m_block[ m_last + 1 ];

        if( m_last < 4000 )
        {
            /*
             * Use simpleSort(), since the full sorting mechanism
             * has quite a large constant overhead.
             */
            for( i = 0; i <= m_last; i++ )
            {
                m_zptr[ i ] = i;
            }
            m_firstAttempt = false;
            m_workDone = 0;
            m_workLimit = 0;
            simpleSort( 0, m_last, 0 );
        }
        else
        {
            for( i = 0; i <= 255; i++ )
            {
                bigDone[ i ] = false;
            }

            for( i = 0; i <= 65536; i++ )
            {
                m_ftab[ i ] = 0;
            }

            c1 = m_block[ 0 ];
            for( i = 0; i <= m_last; i++ )
            {
                c2 = m_block[ i + 1 ];
                m_ftab[ ( c1 << 8 ) + c2 ]++;
                c1 = c2;
            }

            for( i = 1; i <= 65536; i++ )
            {
                m_ftab[ i ] += m_ftab[ i - 1 ];
            }

            c1 = m_block[ 1 ];
            for( i = 0; i < m_last; i++ )
            {
                c2 = m_block[ i + 2 ];
                j = ( c1 << 8 ) + c2;
                c1 = c2;
                m_ftab[ j ]--;
                m_zptr[ m_ftab[ j ] ] = i;
            }

            j = ( ( m_block[ m_last + 1 ] ) << 8 ) + ( m_block[ 1 ] );
            m_ftab[ j ]--;
            m_zptr[ m_ftab[ j ] ] = m_last;

            /*
             * Now ftab contains the first loc of every small bucket.
             * Calculate the running order, from smallest to largest
             * big bucket.
             */
            for( i = 0; i <= 255; i++ )
            {
                runningOrder[ i ] = i;
            }
            {
                int vv;
                int h = 1;
                do
                {
                    h = 3 * h + 1;
                } while( h <= 256 );
                do
                {
                    h = h / 3;
                    for( i = h; i <= 255; i++ )
                    {
                        vv = runningOrder[ i ];
                        j = i;
                        while( ( m_ftab[ ( ( runningOrder[ j - h ] ) + 1 ) << 8 ]
                            - m_ftab[ ( runningOrder[ j - h ] ) << 8 ] ) >
                            ( m_ftab[ ( ( vv ) + 1 ) << 8 ] - m_ftab[ ( vv ) << 8 ] ) )
                        {
                            runningOrder[ j ] = runningOrder[ j - h ];
                            j = j - h;
                            if( j <= ( h - 1 ) )
                            {
                                break;
                            }
                        }
                        runningOrder[ j ] = vv;
                    }
                } while( h != 1 );
            }

            /*
             * The main sorting loop.
             */
            for( i = 0; i <= 255; i++ )
            {

                /*
                 * Process big buckets, starting with the least full.
                 */
                ss = runningOrder[ i ];

                /*
                 * Complete the big bucket [ss] by quicksorting
                 * any unsorted small buckets [ss, j].  Hopefully
                 * previous pointer-scanning phases have already
                 * completed many of the small buckets [ss, j], so
                 * we don't have to sort them at all.
                 */
                for( j = 0; j <= 255; j++ )
                {
                    sb = ( ss << 8 ) + j;
                    if( !( ( m_ftab[ sb ] & SETMASK ) == SETMASK ) )
                    {
                        int lo = m_ftab[ sb ] & CLEARMASK;
                        int hi = ( m_ftab[ sb + 1 ] & CLEARMASK ) - 1;
                        if( hi > lo )
                        {
                            qSort3( lo, hi, 2 );
                            if( m_workDone > m_workLimit && m_firstAttempt )
                            {
                                return;
                            }
                        }
                        m_ftab[ sb ] |= SETMASK;
                    }
                }

                /*
                 * The ss big bucket is now done.  Record this fact,
                 * and update the quadrant descriptors.  Remember to
                 * update quadrants in the overshoot area too, if
                 * necessary.  The "if (i < 255)" test merely skips
                 * this updating for the last bucket processed, since
                 * updating for the last bucket is pointless.
                 */
                bigDone[ ss ] = true;

                if( i < 255 )
                {
                    int bbStart = m_ftab[ ss << 8 ] & CLEARMASK;
                    int bbSize = ( m_ftab[ ( ss + 1 ) << 8 ] & CLEARMASK ) - bbStart;
                    int shifts = 0;

                    while( ( bbSize >> shifts ) > 65534 )
                    {
                        shifts++;
                    }

                    for( j = 0; j < bbSize; j++ )
                    {
                        int a2update = m_zptr[ bbStart + j ];
                        int qVal = ( j >> shifts );
                        m_quadrant[ a2update ] = qVal;
                        if( a2update < NUM_OVERSHOOT_BYTES )
                        {
                            m_quadrant[ a2update + m_last + 1 ] = qVal;
                        }
                    }

                    if( !( ( ( bbSize - 1 ) >> shifts ) <= 65535 ) )
                    {
                        panic();
                    }
                }

                /*
                 * Now scan this big bucket so as to synthesise the
                 * sorted order for small buckets [t, ss] for all t != ss.
                 */
                for( j = 0; j <= 255; j++ )
                {
                    copy[ j ] = m_ftab[ ( j << 8 ) + ss ] & CLEARMASK;
                }

                for( j = m_ftab[ ss << 8 ] & CLEARMASK;
                     j < ( m_ftab[ ( ss + 1 ) << 8 ] & CLEARMASK ); j++ )
                {
                    c1 = m_block[ m_zptr[ j ] ];
                    if( !bigDone[ c1 ] )
                    {
                        m_zptr[ copy[ c1 ] ] = m_zptr[ j ] == 0 ? m_last : m_zptr[ j ] - 1;
                        copy[ c1 ]++;
                    }
                }

                for( j = 0; j <= 255; j++ )
                {
                    m_ftab[ ( j << 8 ) + ss ] |= SETMASK;
                }
            }
        }
    }

    private void makeMaps()
    {
        int i;
        m_nInUse = 0;
        for( i = 0; i < 256; i++ )
        {
            if( m_inUse[ i ] )
            {
                m_seqToUnseq[ m_nInUse ] = (char)i;
                m_unseqToSeq[ i ] = (char)m_nInUse;
                m_nInUse++;
            }
        }
    }

    private char med3( char a, char b, char c )
    {
        char t;
        if( a > b )
        {
            t = a;
            a = b;
            b = t;
        }
        if( b > c )
        {
            t = b;
            b = c;
            c = t;
        }
        if( a > b )
        {
            b = a;
        }
        return b;
    }

    private void moveToFrontCodeAndSend()
        throws IOException
    {
        bsPutIntVS( 24, m_origPtr );
        generateMTFValues();
        sendMTFValues();
    }

    private void qSort3( int loSt, int hiSt, int dSt )
    {
        int unLo;
        int unHi;
        int ltLo;
        int gtHi;
        int med;
        int n;
        int m;
        int sp;
        int lo;
        int hi;
        int d;
        StackElem[] stack = new StackElem[ QSORT_STACK_SIZE ];
        for( int count = 0; count < QSORT_STACK_SIZE; count++ )
        {
            stack[ count ] = new StackElem();
        }

        sp = 0;

        stack[ sp ].m_ll = loSt;
        stack[ sp ].m_hh = hiSt;
        stack[ sp ].m_dd = dSt;
        sp++;

        while( sp > 0 )
        {
            if( sp >= QSORT_STACK_SIZE )
            {
                panic();
            }

            sp--;
            lo = stack[ sp ].m_ll;
            hi = stack[ sp ].m_hh;
            d = stack[ sp ].m_dd;

            if( hi - lo < SMALL_THRESH || d > DEPTH_THRESH )
            {
                simpleSort( lo, hi, d );
                if( m_workDone > m_workLimit && m_firstAttempt )
                {
                    return;
                }
                continue;
            }

            med = med3( m_block[ m_zptr[ lo ] + d + 1 ],
                        m_block[ m_zptr[ hi ] + d + 1 ],
                        m_block[ m_zptr[ ( lo + hi ) >> 1 ] + d + 1 ] );

            unLo = lo;
            ltLo = lo;
            unHi = hi;
            gtHi = hi;

            while( true )
            {
                while( true )
                {
                    if( unLo > unHi )
                    {
                        break;
                    }
                    n = m_block[ m_zptr[ unLo ] + d + 1 ] - med;
                    if( n == 0 )
                    {
                        int temp = 0;
                        temp = m_zptr[ unLo ];
                        m_zptr[ unLo ] = m_zptr[ ltLo ];
                        m_zptr[ ltLo ] = temp;
                        ltLo++;
                        unLo++;
                        continue;
                    }
                    ;
                    if( n > 0 )
                    {
                        break;
                    }
                    unLo++;
                }
                while( true )
                {
                    if( unLo > unHi )
                    {
                        break;
                    }
                    n = m_block[ m_zptr[ unHi ] + d + 1 ] - med;
                    if( n == 0 )
                    {
                        int temp = 0;
                        temp = m_zptr[ unHi ];
                        m_zptr[ unHi ] = m_zptr[ gtHi ];
                        m_zptr[ gtHi ] = temp;
                        gtHi--;
                        unHi--;
                        continue;
                    }
                    ;
                    if( n < 0 )
                    {
                        break;
                    }
                    unHi--;
                }
                if( unLo > unHi )
                {
                    break;
                }
                int temp = 0;
                temp = m_zptr[ unLo ];
                m_zptr[ unLo ] = m_zptr[ unHi ];
                m_zptr[ unHi ] = temp;
                unLo++;
                unHi--;
            }

            if( gtHi < ltLo )
            {
                stack[ sp ].m_ll = lo;
                stack[ sp ].m_hh = hi;
                stack[ sp ].m_dd = d + 1;
                sp++;
                continue;
            }

            n = ( ( ltLo - lo ) < ( unLo - ltLo ) ) ? ( ltLo - lo ) : ( unLo - ltLo );
            vswap( lo, unLo - n, n );
            m = ( ( hi - gtHi ) < ( gtHi - unHi ) ) ? ( hi - gtHi ) : ( gtHi - unHi );
            vswap( unLo, hi - m + 1, m );

            n = lo + unLo - ltLo - 1;
            m = hi - ( gtHi - unHi ) + 1;

            stack[ sp ].m_ll = lo;
            stack[ sp ].m_hh = n;
            stack[ sp ].m_dd = d;
            sp++;

            stack[ sp ].m_ll = n + 1;
            stack[ sp ].m_hh = m - 1;
            stack[ sp ].m_dd = d + 1;
            sp++;

            stack[ sp ].m_ll = m;
            stack[ sp ].m_hh = hi;
            stack[ sp ].m_dd = d;
            sp++;
        }
    }

    private void randomiseBlock()
    {
        int i;
        int rNToGo = 0;
        int rTPos = 0;
        for( i = 0; i < 256; i++ )
        {
            m_inUse[ i ] = false;
        }

        for( i = 0; i <= m_last; i++ )
        {
            if( rNToGo == 0 )
            {
                rNToGo = (char)RAND_NUMS[ rTPos ];
                rTPos++;
                if( rTPos == 512 )
                {
                    rTPos = 0;
                }
            }
            rNToGo--;
            m_block[ i + 1 ] ^= ( ( rNToGo == 1 ) ? 1 : 0 );
            // handle 16 bit signed numbers
            m_block[ i + 1 ] &= 0xFF;

            m_inUse[ m_block[ i + 1 ] ] = true;
        }
    }

    private void sendMTFValues()
        throws IOException
    {
        char[][] len = new char[ N_GROUPS ][ MAX_ALPHA_SIZE ];

        int v;

        int t;

        int i;

        int j;

        int gs;

        int ge;

        int bt;

        int bc;

        int iter;
        int nSelectors = 0;
        int alphaSize;
        int minLen;
        int maxLen;
        int selCtr;
        int nGroups;

        alphaSize = m_nInUse + 2;
        for( t = 0; t < N_GROUPS; t++ )
        {
            for( v = 0; v < alphaSize; v++ )
            {
                len[ t ][ v ] = (char)GREATER_ICOST;
            }
        }

        /*
         * Decide how many coding tables to use
         */
        if( m_nMTF <= 0 )
        {
            panic();
        }

        if( m_nMTF < 200 )
        {
            nGroups = 2;
        }
        else if( m_nMTF < 600 )
        {
            nGroups = 3;
        }
        else if( m_nMTF < 1200 )
        {
            nGroups = 4;
        }
        else if( m_nMTF < 2400 )
        {
            nGroups = 5;
        }
        else
        {
            nGroups = 6;
        }
        {
            /*
             * Generate an initial set of coding tables
             */
            int nPart;
            int remF;
            int tFreq;
            int aFreq;

            nPart = nGroups;
            remF = m_nMTF;
            gs = 0;
            while( nPart > 0 )
            {
                tFreq = remF / nPart;
                ge = gs - 1;
                aFreq = 0;
                while( aFreq < tFreq && ge < alphaSize - 1 )
                {
                    ge++;
                    aFreq += m_mtfFreq[ ge ];
                }

                if( ge > gs && nPart != nGroups && nPart != 1
                    && ( ( nGroups - nPart ) % 2 == 1 ) )
                {
                    aFreq -= m_mtfFreq[ ge ];
                    ge--;
                }

                for( v = 0; v < alphaSize; v++ )
                {
                    if( v >= gs && v <= ge )
                    {
                        len[ nPart - 1 ][ v ] = (char)LESSER_ICOST;
                    }
                    else
                    {
                        len[ nPart - 1 ][ v ] = (char)GREATER_ICOST;
                    }
                }

                nPart--;
                gs = ge + 1;
                remF -= aFreq;
            }
        }

        int[][] rfreq = new int[ N_GROUPS ][ MAX_ALPHA_SIZE ];
        int[] fave = new int[ N_GROUPS ];
        short[] cost = new short[ N_GROUPS ];
        /*
         * Iterate up to N_ITERS times to improve the tables.
         */
        for( iter = 0; iter < N_ITERS; iter++ )
        {
            for( t = 0; t < nGroups; t++ )
            {
                fave[ t ] = 0;
            }

            for( t = 0; t < nGroups; t++ )
            {
                for( v = 0; v < alphaSize; v++ )
                {
                    rfreq[ t ][ v ] = 0;
                }
            }

            nSelectors = 0;
            gs = 0;
            while( true )
            {

                /*
                 * Set group start & end marks.
                 */
                if( gs >= m_nMTF )
                {
                    break;
                }
                ge = gs + G_SIZE - 1;
                if( ge >= m_nMTF )
                {
                    ge = m_nMTF - 1;
                }

                /*
                 * Calculate the cost of this group as coded
                 * by each of the coding tables.
                 */
                for( t = 0; t < nGroups; t++ )
                {
                    cost[ t ] = 0;
                }

                if( nGroups == 6 )
                {
                    short cost0 = 0;
                    short cost1 = 0;
                    short cost2 = 0;
                    short cost3 = 0;
                    short cost4 = 0;
                    short cost5 = 0;

                    for( i = gs; i <= ge; i++ )
                    {
                        short icv = m_szptr[ i ];
                        cost0 += len[ 0 ][ icv ];
                        cost1 += len[ 1 ][ icv ];
                        cost2 += len[ 2 ][ icv ];
                        cost3 += len[ 3 ][ icv ];
                        cost4 += len[ 4 ][ icv ];
                        cost5 += len[ 5 ][ icv ];
                    }
                    cost[ 0 ] = cost0;
                    cost[ 1 ] = cost1;
                    cost[ 2 ] = cost2;
                    cost[ 3 ] = cost3;
                    cost[ 4 ] = cost4;
                    cost[ 5 ] = cost5;
                }
                else
                {
                    for( i = gs; i <= ge; i++ )
                    {
                        short icv = m_szptr[ i ];
                        for( t = 0; t < nGroups; t++ )
                        {
                            cost[ t ] += len[ t ][ icv ];
                        }
                    }
                }

                /*
                 * Find the coding table which is best for this group,
                 * and record its identity in the selector table.
                 */
                bc = 999999999;
                bt = -1;
                for( t = 0; t < nGroups; t++ )
                {
                    if( cost[ t ] < bc )
                    {
                        bc = cost[ t ];
                        bt = t;
                    }
                }
                ;
                fave[ bt ]++;
                m_selector[ nSelectors ] = (char)bt;
                nSelectors++;

                /*
                 * Increment the symbol frequencies for the selected table.
                 */
                for( i = gs; i <= ge; i++ )
                {
                    rfreq[ bt ][ m_szptr[ i ] ]++;
                }

                gs = ge + 1;
            }

            /*
             * Recompute the tables based on the accumulated frequencies.
             */
            for( t = 0; t < nGroups; t++ )
            {
                hbMakeCodeLengths( len[ t ], rfreq[ t ], alphaSize, 20 );
            }
        }

        rfreq = null;
        fave = null;
        cost = null;

        if( !( nGroups < 8 ) )
        {
            panic();
        }
        if( !( nSelectors < 32768 && nSelectors <= ( 2 + ( 900000 / G_SIZE ) ) ) )
        {
            panic();
        }
        {
            /*
             * Compute MTF values for the selectors.
             */
            char[] pos = new char[ N_GROUPS ];
            char ll_i;
            char tmp2;
            char tmp;
            for( i = 0; i < nGroups; i++ )
            {
                pos[ i ] = (char)i;
            }
            for( i = 0; i < nSelectors; i++ )
            {
                ll_i = m_selector[ i ];
                j = 0;
                tmp = pos[ j ];
                while( ll_i != tmp )
                {
                    j++;
                    tmp2 = tmp;
                    tmp = pos[ j ];
                    pos[ j ] = tmp2;
                }
                pos[ 0 ] = tmp;
                m_selectorMtf[ i ] = (char)j;
            }
        }

        int[][] code = new int[ N_GROUPS ][ MAX_ALPHA_SIZE ];

        /*
         * Assign actual codes for the tables.
         */
        for( t = 0; t < nGroups; t++ )
        {
            minLen = 32;
            maxLen = 0;
            for( i = 0; i < alphaSize; i++ )
            {
                if( len[ t ][ i ] > maxLen )
                {
                    maxLen = len[ t ][ i ];
                }
                if( len[ t ][ i ] < minLen )
                {
                    minLen = len[ t ][ i ];
                }
            }
            if( maxLen > 20 )
            {
                panic();
            }
            if( minLen < 1 )
            {
                panic();
            }
            hbAssignCodes( code[ t ], len[ t ], minLen, maxLen, alphaSize );
        }
        {
            /*
             * Transmit the mapping table.
             */
            boolean[] inUse16 = new boolean[ 16 ];
            for( i = 0; i < 16; i++ )
            {
                inUse16[ i ] = false;
                for( j = 0; j < 16; j++ )
                {
                    if( m_inUse[ i * 16 + j ] )
                    {
                        inUse16[ i ] = true;
                    }
                }
            }

            for( i = 0; i < 16; i++ )
            {
                if( inUse16[ i ] )
                {
                    bsW( 1, 1 );
                }
                else
                {
                    bsW( 1, 0 );
                }
            }

            for( i = 0; i < 16; i++ )
            {
                if( inUse16[ i ] )
                {
                    for( j = 0; j < 16; j++ )
                    {
                        if( m_inUse[ i * 16 + j ] )
                        {
                            bsW( 1, 1 );
                        }
                        else
                        {
                            bsW( 1, 0 );
                        }
                    }
                }
            }

        }

        /*
         * Now the selectors.
         */
        bsW( 3, nGroups );
        bsW( 15, nSelectors );
        for( i = 0; i < nSelectors; i++ )
        {
            for( j = 0; j < m_selectorMtf[ i ]; j++ )
            {
                bsW( 1, 1 );
            }
            bsW( 1, 0 );
        }

        for( t = 0; t < nGroups; t++ )
        {
            int curr = len[ t ][ 0 ];
            bsW( 5, curr );
            for( i = 0; i < alphaSize; i++ )
            {
                while( curr < len[ t ][ i ] )
                {
                    bsW( 2, 2 );
                    curr++;
                    /*
                     * 10
                     */
                }
                while( curr > len[ t ][ i ] )
                {
                    bsW( 2, 3 );
                    curr--;
                    /*
                     * 11
                     */
                }
                bsW( 1, 0 );
            }
        }

        /*
         * And finally, the block data proper
         */
        selCtr = 0;
        gs = 0;
        while( true )
        {
            if( gs >= m_nMTF )
            {
                break;
            }
            ge = gs + G_SIZE - 1;
            if( ge >= m_nMTF )
            {
                ge = m_nMTF - 1;
            }
            for( i = gs; i <= ge; i++ )
            {
                bsW( len[ m_selector[ selCtr ] ][ m_szptr[ i ] ],
                     code[ m_selector[ selCtr ] ][ m_szptr[ i ] ] );
            }

            gs = ge + 1;
            selCtr++;
        }
        if( !( selCtr == nSelectors ) )
        {
            panic();
        }
    }

    private void simpleSort( int lo, int hi, int d )
    {
        int i;
        int j;
        int h;
        int bigN;
        int hp;
        int v;

        bigN = hi - lo + 1;
        if( bigN < 2 )
        {
            return;
        }

        hp = 0;
        while( m_incs[ hp ] < bigN )
        {
            hp++;
        }
        hp--;

        for( ; hp >= 0; hp-- )
        {
            h = m_incs[ hp ];

            i = lo + h;
            while( true )
            {
                /*
                 * copy 1
                 */
                if( i > hi )
                {
                    break;
                }
                v = m_zptr[ i ];
                j = i;
                while( fullGtU( m_zptr[ j - h ] + d, v + d ) )
                {
                    m_zptr[ j ] = m_zptr[ j - h ];
                    j = j - h;
                    if( j <= ( lo + h - 1 ) )
                    {
                        break;
                    }
                }
                m_zptr[ j ] = v;
                i++;

                /*
                 * copy 2
                 */
                if( i > hi )
                {
                    break;
                }
                v = m_zptr[ i ];
                j = i;
                while( fullGtU( m_zptr[ j - h ] + d, v + d ) )
                {
                    m_zptr[ j ] = m_zptr[ j - h ];
                    j = j - h;
                    if( j <= ( lo + h - 1 ) )
                    {
                        break;
                    }
                }
                m_zptr[ j ] = v;
                i++;

                /*
                 * copy 3
                 */
                if( i > hi )
                {
                    break;
                }
                v = m_zptr[ i ];
                j = i;
                while( fullGtU( m_zptr[ j - h ] + d, v + d ) )
                {
                    m_zptr[ j ] = m_zptr[ j - h ];
                    j = j - h;
                    if( j <= ( lo + h - 1 ) )
                    {
                        break;
                    }
                }
                m_zptr[ j ] = v;
                i++;

                if( m_workDone > m_workLimit && m_firstAttempt )
                {
                    return;
                }
            }
        }
    }

    private void vswap( int p1, int p2, int n )
    {
        int temp = 0;
        while( n > 0 )
        {
            temp = m_zptr[ p1 ];
            m_zptr[ p1 ] = m_zptr[ p2 ];
            m_zptr[ p2 ] = temp;
            p1++;
            p2++;
            n--;
        }
    }

    private void writeRun()
        throws IOException
    {
        if( m_last < m_allowableBlockSize )
        {
            m_inUse[ m_currentChar ] = true;
            for( int i = 0; i < m_runLength; i++ )
            {
                m_crc.updateCRC( (char)m_currentChar );
            }
            switch( m_runLength )
            {
                case 1:
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    break;
                case 2:
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    break;
                case 3:
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    break;
                default:
                    m_inUse[ m_runLength - 4 ] = true;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)m_currentChar;
                    m_last++;
                    m_block[ m_last + 1 ] = (char)( m_runLength - 4 );
                    break;
            }
        }
        else
        {
            endBlock();
            initBlock();
            writeRun();
        }
    }

    private static class StackElem
    {
        int m_dd;
        int m_hh;
        int m_ll;
    }
}

