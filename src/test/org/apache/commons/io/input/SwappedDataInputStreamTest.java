/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/input/SwappedDataInputStreamTest.java,v 1.5 2004/01/02 08:04:09 bayard Exp $
 * $Revision: 1.5 $
 * $Date: 2004/01/02 08:04:09 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2004 The Apache Software Foundation.  All rights
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


package org.apache.commons.io.input;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * Test for the SwappedDataInputStream. This also 
 * effectively tests the underlying EndianUtils Stream methods.
 *
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision: 1.5 $ $Date: 2004/01/02 08:04:09 $
 */

public class SwappedDataInputStreamTest extends TestCase {

    private SwappedDataInputStream sdis;

    public SwappedDataInputStreamTest(String name) {
        super(name);
    }

    public void setUp() {
        byte[] bytes = new byte[] {
            0x01,
            0x02,
            0x03,
            0x04,
            0x05,
            0x06,
            0x07,
            0x08
        };
        ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
        this.sdis = new SwappedDataInputStream( bais );
    }

    public void tearDown() {
        this.sdis = null;
    }

    public void testReadBoolean() throws IOException {
        assertEquals( false, this.sdis.readBoolean() );
    }

    public void testReadByte() throws IOException {
        assertEquals( 0x01, this.sdis.readByte() );
    }

    public void testReadChar() throws IOException {
        assertEquals( (char) 0x0201, this.sdis.readChar() );
    }

    public void testReadDouble() throws IOException {
        assertEquals( Double.longBitsToDouble(0x0807060504030201L), this.sdis.readDouble(), 0 );
    }

    public void testReadFloat() throws IOException {
        assertEquals( Float.intBitsToFloat(0x04030201), this.sdis.readFloat(), 0 );
    }

    /*
    public void testReadFully() throws IOException {
    }
    */

    public void testReadInt() throws IOException {
        assertEquals( (int) 0x04030201, this.sdis.readInt() );
    }

    /*
    public void testReadLine() throws IOException {
    }
    */

    public void testReadLong() throws IOException {
        assertEquals( 0x0807060504030201L, this.sdis.readLong() );
    }

    public void testReadShort() throws IOException {
        assertEquals( (short) 0x0201, this.sdis.readShort() );
    }

    /*
    public void testReadUnsignedByte() throws IOException {
    }

    public void testReadUnsignedShort() throws IOException {
    }
    */

    public void testReadUTF() throws IOException {
        try {
            String unexpected = this.sdis.readUTF();
            fail("readUTF should be unsupported. ");
        } catch(UnsupportedOperationException uoe) {
        }
    }

    public void testSkipBytes() throws IOException {
        this.sdis.skipBytes(4);
        assertEquals( (int)0x08070605, this.sdis.readInt() );
    }

}
