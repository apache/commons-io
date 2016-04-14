/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.serialization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class RegexpClassNameMatcherTest {

    @Test
    public void testSimplePatternFromString() {
        ClassNameMatcher ca = new RegexpClassNameMatcher("foo.*");
        assertTrue(ca.matches("foo.should.match"));
        assertFalse(ca.matches("bar.should.not.match"));
    }

    @Test
    public void testSimplePatternFromPattern() {
        ClassNameMatcher ca = new RegexpClassNameMatcher(Pattern.compile("foo.*"));
        assertTrue(ca.matches("foo.should.match"));
        assertFalse(ca.matches("bar.should.not.match"));
    }

    @Test
    public void testOrPattern() {
        ClassNameMatcher ca = new RegexpClassNameMatcher("foo.*|bar.*");
        assertTrue(ca.matches("foo.should.match"));
        assertTrue(ca.matches("bar.should.match"));
        assertFalse(ca.matches("zoo.should.not.match"));
    }

    @Test(expected=NullPointerException.class)
    public void testNullStringPattern() {
        new RegexpClassNameMatcher((String)null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullPatternPattern() {
        new RegexpClassNameMatcher((Pattern)null);
    }
}