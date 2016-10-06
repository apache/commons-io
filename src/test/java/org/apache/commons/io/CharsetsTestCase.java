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

package org.apache.commons.io;

import java.nio.charset.Charset;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Charsets}.
 * 
 * @version $Id: CharEncodingTest.java 1298985 2012-03-09 19:12:49Z ggregory $
 */
public class CharsetsTestCase {

    @Test
    public void testRequiredCharsets() {
        final SortedMap<String, Charset> requiredCharsets = Charsets.requiredCharsets();
        // test for what we expect to be there as of Java 6
        // Make sure the object at the given key is the right one
        Assert.assertEquals(requiredCharsets.get("US-ASCII").name(), "US-ASCII");
        Assert.assertEquals(requiredCharsets.get("ISO-8859-1").name(), "ISO-8859-1");
        Assert.assertEquals(requiredCharsets.get("UTF-8").name(), "UTF-8");
        Assert.assertEquals(requiredCharsets.get("UTF-16").name(), "UTF-16");
        Assert.assertEquals(requiredCharsets.get("UTF-16BE").name(), "UTF-16BE");
        Assert.assertEquals(requiredCharsets.get("UTF-16LE").name(), "UTF-16LE");
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testIso8859_1() {
        Assert.assertEquals("ISO-8859-1", Charsets.ISO_8859_1.name());
    }

    @Test
    public void testToCharset() {
        Assert.assertEquals(Charset.defaultCharset(), Charsets.toCharset((String) null));
        Assert.assertEquals(Charset.defaultCharset(), Charsets.toCharset((Charset) null));
        Assert.assertEquals(Charset.defaultCharset(), Charsets.toCharset(Charset.defaultCharset()));
        Assert.assertEquals(Charset.forName("UTF-8"), Charsets.toCharset(Charset.forName("UTF-8")));
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testUsAscii() {
        Assert.assertEquals("US-ASCII", Charsets.US_ASCII.name());
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testUtf16() {
        Assert.assertEquals("UTF-16", Charsets.UTF_16.name());
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testUtf16Be() {
        Assert.assertEquals("UTF-16BE", Charsets.UTF_16BE.name());
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testUtf16Le() {
        Assert.assertEquals("UTF-16LE", Charsets.UTF_16LE.name());
    }

    @Test
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    public void testUtf8() {
        Assert.assertEquals("UTF-8", Charsets.UTF_8.name());
    }

}
