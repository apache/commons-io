/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.bzip2;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that decompresses from the BZip2 format (without the file
 * header chars) to be read as any other stream.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class CBZip2InputStream
    extends InputStream
    implements BZip2Constants
{
    private static final int START_BLOCK_STATE = 1;
    private static final int RAND_PART_A_STATE = 2;
    private static final int RAND_PART_B_STATE = 3;
    private static final int RAND_PART_C_STATE = 4;
    private static final int NO_RAND_PART_A_STATE = 5;
    private static final int NO_RAND_PART_B_STATE = 6;
    private static final int NO_RAND_PART_C_STATE = 7;

    private CRC m_crc = new CRC();
    private boolean[] m_inUse = new boolean[ 256 ];
    private char[] m_seqToUnseq = new char[ 256 ];
    private char[] m_unseqToSeq = new char[ 256 ];
    private char[] m_selector = new char[ MAX_SELECTORS ];
    private char[] m_selectorMtf = new char[ MAX_SELECTORS ];

    /*
     * freq table collected to save a pass over the data
     * during decompression.
     */
    private int[] m_unzftab = new int[ 256 ];

    private int[][] m_limit = new int[ N_GROUPS ][ MAX_ALPHA_SIZE ];
    private int[][] m_base = new int[ N_GROUPS ][ MAX_ALPHA_SIZE ];
    private int[][] m_perm = new int[ N_GROUPS ][ MAX_ALPHA_SIZE ];
    private int[] m_minLens = new int[ N_GROUPS ];

    private boolean m_streamEnd;
    private int m_currentChar = -1;

    private int m_currentState = START_BLOCK_STATE;
    private int m_rNToGo;
    private int m_rTPos;
    private int m_tPos;

    private int i2;
    private int count;
    private int chPrev;
    private int ch2;
    private int j2;
    private char z;

    private boolean m_blockRandomised;

    /*
     * always: in the range 0 .. 9.
     * The current block size is 100000 * this number.
     */
    private int m_blockSize100k;
    private int m_bsBuff;
    private int m_bsLive;

    private InputStream m_input;

    private int m_computedBlockCRC;
    private int m_computedCombinedCRC;

    /*
     * index of the last char in the block, so
     * the block size == last + 1.
     */
    private int m_last;
    private char[] m_ll8;
    private int m_nInUse;

    /*
     * index in zptr[] of original string after sorting.
     */
    private int m_origPtr;

    private int m_storedBlockCRC;
    private int m_storedCombinedCRC;
    private int[] m_tt;

    public CBZip2InputStream( final InputStream input )
    {
        bsSetStream( input );
        initialize();
        initBlock();
        setupBlock();
    }

    private static void badBlockHeader()
    {
        cadvise();
    }

    private static void blockOverrun()
    {
        cadvise();
    }

    private static void cadvise()
    {
        System.out.println( "CRC Error" );
        //throw new CCoruptionError();
    }

    private static void compressedStreamEOF()
    {
        cadvise();
    }

    private static void crcError()
    {
        cadvise();
    }

    public int read()
    {
        if( m_streamEnd )
        {
            return -1;
        }
        else
        {
            int retChar = m_currentChar;
            switch( m_currentState )
            {
                case START_BLOCK_STATE:
                    break;
                case RAND_PART_A_STATE:
                    break;
                case RAND_PART_B_STATE:
                    setupRandPartB();
                    break;
                case RAND_PART_C_STATE:
                    setupRandPartC();
                    break;
                case NO_RAND_PART_A_STATE:
                    break;
                case NO_RAND_PART_B_STATE:
                    setupNoRandPartB();
                    break;
                case NO_RAND_PART_C_STATE:
                    setupNoRandPartC();
                    break;
                default:
                    break;
            }
            return retChar;
        }
    }

    private void setDecompressStructureSizes( int newSize100k )
    {
        if( !( 0 <= newSize100k && newSize100k <= 9 && 0 <= m_blockSize100k
            && m_blockSize100k <= 9 ) )
        {
            // throw new IOException("Invalid block size");
        }

        m_blockSize100k = newSize100k;

        if( newSize100k == 0 )
        {
            return;
        }

        int n = BASE_BLOCK_SIZE * newSize100k;
        m_ll8 = new char[ n ];
        m_tt = new int[ n ];
    }

    private void setupBlock()
    {
        int[] cftab = new int[ 257 ];
        char ch;

        cftab[ 0 ] = 0;
        for( int i = 1; i <= 256; i++ )
        {
            cftab[ i ] = m_unzftab[ i - 1 ];
        }
        for( int i = 1; i <= 256; i++ )
        {
            cftab[ i ] += cftab[ i - 1 ];
        }

        for( int i = 0; i <= m_last; i++ )
        {
            ch = m_ll8[ i ];
            m_tt[ cftab[ ch ] ] = i;
            cftab[ ch ]++;
        }
        cftab = null;

        m_tPos = m_tt[ m_origPtr ];

        count = 0;
        i2 = 0;
        ch2 = 256;
        /*
         * not a char and not EOF
         */
        if( m_blockRandomised )
        {
            m_rNToGo = 0;
            m_rTPos = 0;
            setupRandPartA();
        }
        else
        {
            setupNoRandPartA();
        }
    }

    private void setupNoRandPartA()
    {
        if( i2 <= m_last )
        {
            chPrev = ch2;
            ch2 = m_ll8[ m_tPos ];
            m_tPos = m_tt[ m_tPos ];
            i2++;

            m_currentChar = ch2;
            m_currentState = NO_RAND_PART_B_STATE;
            m_crc.updateCRC( ch2 );
        }
        else
        {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    private void setupNoRandPartB()
    {
        if( ch2 != chPrev )
        {
            m_currentState = NO_RAND_PART_A_STATE;
            count = 1;
            setupNoRandPartA();
        }
        else
        {
            count++;
            if( count >= 4 )
            {
                z = m_ll8[ m_tPos ];
                m_tPos = m_tt[ m_tPos ];
                m_currentState = NO_RAND_PART_C_STATE;
                j2 = 0;
                setupNoRandPartC();
            }
            else
            {
                m_currentState = NO_RAND_PART_A_STATE;
                setupNoRandPartA();
            }
        }
    }

    private void setupNoRandPartC()
    {
        if( j2 < z )
        {
            m_currentChar = ch2;
            m_crc.updateCRC( ch2 );
            j2++;
        }
        else
        {
            m_currentState = NO_RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupNoRandPartA();
        }
    }

    private void setupRandPartA()
    {
        if( i2 <= m_last )
        {
            chPrev = ch2;
            ch2 = m_ll8[ m_tPos ];
            m_tPos = m_tt[ m_tPos ];
            if( m_rNToGo == 0 )
            {
                m_rNToGo = RAND_NUMS[ m_rTPos ];
                m_rTPos++;
                if( m_rTPos == 512 )
                {
                    m_rTPos = 0;
                }
            }
            m_rNToGo--;
            ch2 ^= ( ( m_rNToGo == 1 ) ? 1 : 0 );
            i2++;

            m_currentChar = ch2;
            m_currentState = RAND_PART_B_STATE;
            m_crc.updateCRC( ch2 );
        }
        else
        {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    private void setupRandPartB()
    {
        if( ch2 != chPrev )
        {
            m_currentState = RAND_PART_A_STATE;
            count = 1;
            setupRandPartA();
        }
        else
        {
            count++;
            if( count >= 4 )
            {
                z = m_ll8[ m_tPos ];
                m_tPos = m_tt[ m_tPos ];
                if( m_rNToGo == 0 )
                {
                    m_rNToGo = RAND_NUMS[ m_rTPos ];
                    m_rTPos++;
                    if( m_rTPos == 512 )
                    {
                        m_rTPos = 0;
                    }
                }
                m_rNToGo--;
                z ^= ( ( m_rNToGo == 1 ) ? 1 : 0 );
                j2 = 0;
                m_currentState = RAND_PART_C_STATE;
                setupRandPartC();
            }
            else
            {
                m_currentState = RAND_PART_A_STATE;
                setupRandPartA();
            }
        }
    }

    private void setupRandPartC()
    {
        if( j2 < z )
        {
            m_currentChar = ch2;
            m_crc.updateCRC( ch2 );
            j2++;
        }
        else
        {
            m_currentState = RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupRandPartA();
        }
    }

    private void getAndMoveToFrontDecode()
    {
        int nextSym;

        int limitLast = BASE_BLOCK_SIZE * m_blockSize100k;
        m_origPtr = readVariableSizedInt( 24 );

        recvDecodingTables();
        int EOB = m_nInUse + 1;
        int groupNo = -1;
        int groupPos = 0;

        /*
         * Setting up the unzftab entries here is not strictly
         * necessary, but it does save having to do it later
         * in a separate pass, and so saves a block's worth of
         * cache misses.
         */
        for( int i = 0; i <= 255; i++ )
        {
            m_unzftab[ i ] = 0;
        }

        final char[] yy = new char[ 256 ];
        for( int i = 0; i <= 255; i++ )
        {
            yy[ i ] = (char)i;
        }

        m_last = -1;
        int zt;
        int zn;
        int zvec;
        int zj;
        groupNo++;
        groupPos = G_SIZE - 1;

        zt = m_selector[ groupNo ];
        zn = m_minLens[ zt ];
        zvec = bsR( zn );
        while( zvec > m_limit[ zt ][ zn ] )
        {
            zn++;

            while( m_bsLive < 1 )
            {
                int zzi;
                char thech = 0;
                try
                {
                    thech = (char)m_input.read();
                }
                catch( IOException e )
                {
                    compressedStreamEOF();
                }
                if( thech == -1 )
                {
                    compressedStreamEOF();
                }
                zzi = thech;
                m_bsBuff = ( m_bsBuff << 8 ) | ( zzi & 0xff );
                m_bsLive += 8;
            }

            zj = ( m_bsBuff >> ( m_bsLive - 1 ) ) & 1;
            m_bsLive--;

            zvec = ( zvec << 1 ) | zj;
        }
        nextSym = m_perm[ zt ][ zvec - m_base[ zt ][ zn ] ];

        while( true )
        {
            if( nextSym == EOB )
            {
                break;
            }

            if( nextSym == RUNA || nextSym == RUNB )
            {
                char ch;
                int s = -1;
                int N = 1;
                do
                {
                    if( nextSym == RUNA )
                    {
                        s = s + ( 0 + 1 ) * N;
                    }
                    else// if( nextSym == RUNB )
                    {
                        s = s + ( 1 + 1 ) * N;
                    }
                    N = N * 2;

                    if( groupPos == 0 )
                    {
                        groupNo++;
                        groupPos = G_SIZE;
                    }
                    groupPos--;
                    zt = m_selector[ groupNo ];
                    zn = m_minLens[ zt ];
                    zvec = bsR( zn );
                    while( zvec > m_limit[ zt ][ zn ] )
                    {
                        zn++;

                        while( m_bsLive < 1 )
                        {
                            int zzi;
                            char thech = 0;
                            try
                            {
                                thech = (char)m_input.read();
                            }
                            catch( IOException e )
                            {
                                compressedStreamEOF();
                            }
                            if( thech == -1 )
                            {
                                compressedStreamEOF();
                            }
                            zzi = thech;
                            m_bsBuff = ( m_bsBuff << 8 ) | ( zzi & 0xff );
                            m_bsLive += 8;
                        }

                        zj = ( m_bsBuff >> ( m_bsLive - 1 ) ) & 1;
                        m_bsLive--;
                        zvec = ( zvec << 1 ) | zj;
                    }

                    nextSym = m_perm[ zt ][ zvec - m_base[ zt ][ zn ] ];

                } while( nextSym == RUNA || nextSym == RUNB );

                s++;
                ch = m_seqToUnseq[ yy[ 0 ] ];
                m_unzftab[ ch ] += s;

                while( s > 0 )
                {
                    m_last++;
                    m_ll8[ m_last ] = ch;
                    s--;
                }

                if( m_last >= limitLast )
                {
                    blockOverrun();
                }
                continue;
            }
            else
            {
                char tmp;
                m_last++;
                if( m_last >= limitLast )
                {
                    blockOverrun();
                }

                tmp = yy[ nextSym - 1 ];
                m_unzftab[ m_seqToUnseq[ tmp ] ]++;
                m_ll8[ m_last ] = m_seqToUnseq[ tmp ];

                /*
                 * This loop is hammered during decompression,
                 * hence the unrolling.
                 * for (j = nextSym-1; j > 0; j--) yy[j] = yy[j-1];
                 */
                int j = nextSym - 1;
                for( ; j > 3; j -= 4 )
                {
                    yy[ j ] = yy[ j - 1 ];
                    yy[ j - 1 ] = yy[ j - 2 ];
                    yy[ j - 2 ] = yy[ j - 3 ];
                    yy[ j - 3 ] = yy[ j - 4 ];
                }
                for( ; j > 0; j-- )
                {
                    yy[ j ] = yy[ j - 1 ];
                }

                yy[ 0 ] = tmp;

                if( groupPos == 0 )
                {
                    groupNo++;
                    groupPos = G_SIZE;
                }
                groupPos--;
                zt = m_selector[ groupNo ];
                zn = m_minLens[ zt ];
                zvec = bsR( zn );
                while( zvec > m_limit[ zt ][ zn ] )
                {
                    zn++;

                    while( m_bsLive < 1 )
                    {
                        char ch = 0;
                        try
                        {
                            ch = (char)m_input.read();
                        }
                        catch( IOException e )
                        {
                            compressedStreamEOF();
                        }

                        m_bsBuff = ( m_bsBuff << 8 ) | ( ch & 0xff );
                        m_bsLive += 8;
                    }

                    zj = ( m_bsBuff >> ( m_bsLive - 1 ) ) & 1;
                    m_bsLive--;

                    zvec = ( zvec << 1 ) | zj;
                }
                nextSym = m_perm[ zt ][ zvec - m_base[ zt ][ zn ] ];

                continue;
            }
        }
    }

    private void bsFinishedWithStream()
    {
        m_input = null;
    }

    private int readVariableSizedInt( final int numBits )
    {
        return bsR( numBits );
    }

    private char readUnsignedChar()
    {
        return (char)bsR( 8 );
    }

    private int readInt()
    {
        int u = 0;
        u = ( u << 8 ) | bsR( 8 );
        u = ( u << 8 ) | bsR( 8 );
        u = ( u << 8 ) | bsR( 8 );
        u = ( u << 8 ) | bsR( 8 );
        return u;
    }

    private int bsR( final int n )
    {
        while( m_bsLive < n )
        {
            char ch = 0;
            try
            {
                ch = (char)m_input.read();
            }
            catch( final IOException ioe )
            {
                compressedStreamEOF();
            }

            if( ch == -1 )
            {
                compressedStreamEOF();
            }

            m_bsBuff = ( m_bsBuff << 8 ) | ( ch & 0xff );
            m_bsLive += 8;
        }

        final int result = ( m_bsBuff >> ( m_bsLive - n ) ) & ( ( 1 << n ) - 1 );
        m_bsLive -= n;
        return result;
    }

    private void bsSetStream( final InputStream input )
    {
        m_input = input;
        m_bsLive = 0;
        m_bsBuff = 0;
    }

    private void complete()
    {
        m_storedCombinedCRC = readInt();
        if( m_storedCombinedCRC != m_computedCombinedCRC )
        {
            crcError();
        }

        bsFinishedWithStream();
        m_streamEnd = true;
    }

    private void endBlock()
    {
        m_computedBlockCRC = m_crc.getFinalCRC();
        /*
         * A bad CRC is considered a fatal error.
         */
        if( m_storedBlockCRC != m_computedBlockCRC )
        {
            crcError();
        }

        m_computedCombinedCRC = ( m_computedCombinedCRC << 1 )
            | ( m_computedCombinedCRC >>> 31 );
        m_computedCombinedCRC ^= m_computedBlockCRC;
    }

    private void hbCreateDecodeTables( final int[] limit,
                                       final int[] base,
                                       final int[] perm,
                                       final char[] length,
                                       final int minLen,
                                       final int maxLen,
                                       final int alphaSize )
    {
        int pp = 0;
        for( int i = minLen; i <= maxLen; i++ )
        {
            for( int j = 0; j < alphaSize; j++ )
            {
                if( length[ j ] == i )
                {
                    perm[ pp ] = j;
                    pp++;
                }
            }
        }

        for( int i = 0; i < MAX_CODE_LEN; i++ )
        {
            base[ i ] = 0;
        }

        for( int i = 0; i < alphaSize; i++ )
        {
            base[ length[ i ] + 1 ]++;
        }

        for( int i = 1; i < MAX_CODE_LEN; i++ )
        {
            base[ i ] += base[ i - 1 ];
        }

        for( int i = 0; i < MAX_CODE_LEN; i++ )
        {
            limit[ i ] = 0;
        }

        int vec = 0;
        for( int i = minLen; i <= maxLen; i++ )
        {
            vec += ( base[ i + 1 ] - base[ i ] );
            limit[ i ] = vec - 1;
            vec <<= 1;
        }

        for( int i = minLen + 1; i <= maxLen; i++ )
        {
            base[ i ] = ( ( limit[ i - 1 ] + 1 ) << 1 ) - base[ i ];
        }
    }

    private void initBlock()
    {
        final char magic1 = readUnsignedChar();
        final char magic2 = readUnsignedChar();
        final char magic3 = readUnsignedChar();
        final char magic4 = readUnsignedChar();
        final char magic5 = readUnsignedChar();
        final char magic6 = readUnsignedChar();
        if( magic1 == 0x17 && magic2 == 0x72 && magic3 == 0x45 &&
            magic4 == 0x38 && magic5 == 0x50 && magic6 == 0x90 )
        {
            complete();
            return;
        }

        if( magic1 != 0x31 || magic2 != 0x41 || magic3 != 0x59 ||
            magic4 != 0x26 || magic5 != 0x53 || magic6 != 0x59 )
        {
            badBlockHeader();
            m_streamEnd = true;
            return;
        }

        m_storedBlockCRC = readInt();

        if( bsR( 1 ) == 1 )
        {
            m_blockRandomised = true;
        }
        else
        {
            m_blockRandomised = false;
        }

        //        currBlockNo++;
        getAndMoveToFrontDecode();

        m_crc.initialiseCRC();
        m_currentState = START_BLOCK_STATE;
    }

    private void initialize()
    {
        final char magic3 = readUnsignedChar();
        final char magic4 = readUnsignedChar();
        if( magic3 != 'h' || magic4 < '1' || magic4 > '9' )
        {
            bsFinishedWithStream();
            m_streamEnd = true;
            return;
        }

        setDecompressStructureSizes( magic4 - '0' );
        m_computedCombinedCRC = 0;
    }

    private void makeMaps()
    {
        m_nInUse = 0;
        for( int i = 0; i < 256; i++ )
        {
            if( m_inUse[ i ] )
            {
                m_seqToUnseq[ m_nInUse ] = (char)i;
                m_unseqToSeq[ i ] = (char)m_nInUse;
                m_nInUse++;
            }
        }
    }

    private void recvDecodingTables()
    {
        buildInUseTable();
        makeMaps();
        final int alphaSize = m_nInUse + 2;

        /*
         * Now the selectors
         */
        final int groupCount = bsR( 3 );
        final int selectorCount = bsR( 15 );
        for( int i = 0; i < selectorCount; i++ )
        {
            int run = 0;
            while( bsR( 1 ) == 1 )
            {
                run++;
            }
            m_selectorMtf[ i ] = (char)run;
        }

        /*
         * Undo the MTF values for the selectors.
         */
        final char[] pos = new char[ N_GROUPS ];
        for( char v = 0; v < groupCount; v++ )
        {
            pos[ v ] = v;
        }

        for( int i = 0; i < selectorCount; i++ )
        {
            int v = m_selectorMtf[ i ];
            final char tmp = pos[ v ];
            while( v > 0 )
            {
                pos[ v ] = pos[ v - 1 ];
                v--;
            }
            pos[ 0 ] = tmp;
            m_selector[ i ] = tmp;
        }

        final char[][] len = new char[ N_GROUPS ][ MAX_ALPHA_SIZE ];
        /*
         * Now the coding tables
         */
        for( int i = 0; i < groupCount; i++ )
        {
            int curr = bsR( 5 );
            for( int j = 0; j < alphaSize; j++ )
            {
                while( bsR( 1 ) == 1 )
                {
                    if( bsR( 1 ) == 0 )
                    {
                        curr++;
                    }
                    else
                    {
                        curr--;
                    }
                }
                len[ i ][ j ] = (char)curr;
            }
        }

        /*
         * Create the Huffman decoding tables
         */
        for( int k = 0; k < groupCount; k++ )
        {
            int minLen = 32;
            int maxLen = 0;
            for( int i = 0; i < alphaSize; i++ )
            {
                if( len[ k ][ i ] > maxLen )
                {
                    maxLen = len[ k ][ i ];
                }
                if( len[ k ][ i ] < minLen )
                {
                    minLen = len[ k ][ i ];
                }
            }
            hbCreateDecodeTables( m_limit[ k ], m_base[ k ], m_perm[ k ], len[ k ], minLen,
                                  maxLen, alphaSize );
            m_minLens[ k ] = minLen;
        }
    }

    private void buildInUseTable()
    {
        final boolean[] inUse16 = new boolean[ 16 ];

        /*
         * Receive the mapping table
         */
        for( int i = 0; i < 16; i++ )
        {
            if( bsR( 1 ) == 1 )
            {
                inUse16[ i ] = true;
            }
            else
            {
                inUse16[ i ] = false;
            }
        }

        for( int i = 0; i < 256; i++ )
        {
            m_inUse[ i ] = false;
        }

        for( int i = 0; i < 16; i++ )
        {
            if( inUse16[ i ] )
            {
                for( int j = 0; j < 16; j++ )
                {
                    if( bsR( 1 ) == 1 )
                    {
                        m_inUse[ i * 16 + j ] = true;
                    }
                }
            }
        }
    }
}
