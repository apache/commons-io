/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/input/CountingInputStreamTest.java,v 1.2 2003/12/30 07:00:04 bayard Exp $
 * $Revision: 1.2 $
 * $Date: 2003/12/30 07:00:04 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

/**
 * Tests the CountingInputStream.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 */
public class CountingInputStreamTest extends TestCase {

    public CountingInputStreamTest(String name) {
        super(name);
    }

    public void testCounting() throws Exception {
        String text = "A piece of text";
        byte[] bytes = text.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        CountingInputStream cis = new CountingInputStream(bais);

        // have to declare this larger as we're going to read 
        // off the end of the stream and input stream seems 
        // to do bounds checking
        byte[] result = new byte[21];

        byte[] ba = new byte[5];
        int found = cis.read(ba);
        System.arraycopy(ba, 0, result, 0, 5);
        assertEquals( found, cis.getCount() );

        int value = cis.read();
        found++; 
        result[5] = (byte)value;
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 6, 5);
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 11, 10); // off the end
        assertEquals( found, cis.getCount() );

        // trim to get rid of the 6 empty values
        String textResult = new String(result).trim();
        assertEquals(textResult, text);
    }
}

