/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/output/TeeOutputStreamTest.java,v 1.2 2003/11/27 06:57:44 bayard Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/27 06:57:44 $
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


package org.apache.commons.io.output;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision: 1.2 $ $Date: 2003/11/27 06:57:44 $
 */

public class TeeOutputStreamTest extends TestCase {

    public TeeOutputStreamTest(String name) {
        super(name);
    }

    public void testTee() throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TeeOutputStream tos = new TeeOutputStream(baos1, baos2);
        for(int i = 0; i < 20; i++) {
            tos.write(i);
        }
        assertByteArrayEquals("TeeOutputStream.write(int)", baos1.toByteArray(), baos2.toByteArray() );

        byte[] array = new byte[10];
        for(int i = 20; i < 30; i++) {
            array[i-20] = (byte)i;
        }
        tos.write(array);
        assertByteArrayEquals("TeeOutputStream.write(byte[])", baos1.toByteArray(), baos2.toByteArray() );

        for(int i = 25; i < 35; i++) {
            array[i-25] = (byte)i;
        }
        tos.write(array, 5, 5);
        assertByteArrayEquals("TeeOutputStream.write(byte[], int, int)", baos1.toByteArray(), baos2.toByteArray() );
    }

    private void assertByteArrayEquals(String msg, byte[] array1, byte[] array2) {
        assertEquals(msg+": array size mismatch", array1.length, array2.length);
        for(int i=0; i<array1.length; i++) {
            assertEquals(msg+": array[ " + i + "] mismatch", array1[i], array2[i]);
        }
    }

}
