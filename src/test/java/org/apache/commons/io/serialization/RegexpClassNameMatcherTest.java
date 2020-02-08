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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class RegexpClassNameMatcherTest {

    @Test
    public void testSimplePatternFromString() {
        final ClassNameMatcher ca = new RegexpClassNameMatcher("foo.*");
        assertTrue(ca.matches("foo.should.match"));
        assertFalse(ca.matches("bar.should.not.match"));
    }

    @Test
    public void testSimplePatternFromPattern() {
        final ClassNameMatcher ca = new RegexpClassNameMatcher(Pattern.compile("foo.*"));
        assertTrue(ca.matches("foo.should.match"));
        assertFalse(ca.matches("bar.should.not.match"));
    }

    @Test
    public void testOrPattern() {
        final ClassNameMatcher ca = new RegexpClassNameMatcher("foo.*|bar.*");
        assertTrue(ca.matches("foo.should.match"));
        assertTrue(ca.matches("bar.should.match"));
        assertFalse(ca.matches("zoo.should.not.match"));
    }

    @Test
    public void testNullStringPattern() {
        assertThrows(NullPointerException.class, () -> new RegexpClassNameMatcher((String)null));
    }

    @Test
    public void testNullPatternPattern() {
        assertThrows(IllegalArgumentException.class, () -> new RegexpClassNameMatcher((Pattern)null));
    }
}