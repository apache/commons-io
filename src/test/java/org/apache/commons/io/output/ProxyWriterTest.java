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
package org.apache.commons.io.output;

import junit.framework.TestCase;

/**
 * Test {@link ProxyWriter}.
 *
 * @version $Id$
 */
public class ProxyWriterTest extends TestCase {

    public ProxyWriterTest(final String name) {
        super(name);
    }

    public void testAppendCharSequence() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.append("ABC");
        } catch(final Exception e) {
            fail("Appending CharSequence threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    public void testWriteString() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC");
        } catch(final Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    public void testWriteStringPartial() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC", 1, 2);
        } catch(final Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("BC", writer.toString());
        proxy.close();
    }

    public void testWriteCharArray() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[] {'A', 'B', 'C'});
        } catch(final Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    public void testWriteCharArrayPartial() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[] {'A', 'B', 'C'}, 1, 2);
        } catch(final Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("BC", writer.toString());
        proxy.close();
    }

    public void testNullString() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((String)null);
        } catch(final Exception e) {
            fail("Writing null String threw " + e);
        }

        try {
            proxy.write((String)null, 0, 0);
        } catch(final Exception e) {
            fail("Writing null String threw " + e);
        }
        proxy.close();
    }

    public void testNullCharArray() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((char[])null);
        } catch(final Exception e) {
            fail("Writing null char[] threw " + e);
        }

        try {
            proxy.write((char[])null, 0, 0);
        } catch(final Exception e) {
            fail("Writing null char[] threw " + e);
        }
        proxy.close();
    }

    public void testNullCharSequencec() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.append((String)null);
        } catch(final Exception e) {
            fail("Appending null CharSequence threw " + e);
        }
        proxy.close();
    }

}
