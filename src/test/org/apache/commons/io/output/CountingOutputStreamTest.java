/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/output/CountingOutputStreamTest.java,v 1.1 2003/11/23 09:32:45 bayard Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/23 09:32:45 $
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
 * @version $Revision: 1.1 $ $Date: 2003/11/23 09:32:45 $
 */

public class CountingOutputStreamTest extends TestCase {

    public CountingOutputStreamTest(String name) {
        super(name);
    }

    public void testCounting() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream cos = new CountingOutputStream(baos);

        for(int i = 0; i < 20; i++) {
            cos.write(i);
        }
        assertByteArrayEquals("CountingOutputStream.write(int)", baos.toByteArray(), 0, 20);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 20);

        byte[] array = new byte[10];
        for(int i = 20; i < 30; i++) {
            array[i-20] = (byte)i;
        }
        cos.write(array);
        assertByteArrayEquals("CountingOutputStream.write(byte[])", baos.toByteArray(), 0, 30);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 30);

        for(int i = 25; i < 35; i++) {
            array[i-25] = (byte)i;
        }
        cos.write(array, 5, 5);
        assertByteArrayEquals("CountingOutputStream.write(byte[], int, int)", baos.toByteArray(), 0, 35);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 35);
    }

    private void assertByteArrayEquals(String msg, byte[] array, int start, int end) {
        assertEquals(msg+": array size mismatch", end-start,
                array.length );

        for (int i = start; i < end; i++) {
            assertEquals(msg+": array[ " + i + "] mismatch", array[i],
                    i);
        }
    }

}
