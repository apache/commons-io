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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.jupiter.api.Test;

public class CharacterSetFilterReaderTest {

    private static final String STRING_FIXTURE = "ab";

    @Test
    public void testInputSize0FilterSize0() throws IOException {
        final StringReader input = new StringReader("");
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, new HashSet<>(0))) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize0FilterSize1() throws IOException {
        final StringReader input = new StringReader("");
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('a'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize0NullFilter() throws IOException {
        final StringReader input = new StringReader("");
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, (Set<Integer>) null)) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize1FilterSize1() throws IOException {
        try (StringReader input = new StringReader("a")) {
            final HashSet<Integer> codePoints = new HashSet<>();
            codePoints.add(Integer.valueOf('a'));
            try (final CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
                assertEquals(-1, reader.read());
            }
        }
    }

    @Test
    public void testInputSize2FilterSize1FilterAll() throws IOException {
        final StringReader input = new StringReader("aa");
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('a'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize2FilterSize1FilterFirst() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('a'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals('b', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize2FilterSize1FilterLast() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('b'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals('a', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize2FilterSize2FilterFirst() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('a'));
        codePoints.add(Integer.valueOf('y'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals('b', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize2FilterSize2FilterLast() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('x'));
        codePoints.add(Integer.valueOf('b'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals('a', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testInputSize2FilterSize2FilterNone() throws IOException {
        final StringReader input = new StringReader(STRING_FIXTURE);
        final HashSet<Integer> codePoints = new HashSet<>();
        codePoints.add(Integer.valueOf('x'));
        codePoints.add(Integer.valueOf('y'));
        try (CharacterSetFilterReader reader = new CharacterSetFilterReader(input, codePoints)) {
            assertEquals('a', reader.read());
            assertEquals('b', reader.read());
        }
    }

    @Test
    public void testReadFilteringEOF() {
        final StringReader input = new StringReader(STRING_FIXTURE);
        assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            try (StringBuilderWriter output = new StringBuilderWriter();
                CharacterSetFilterReader reader = new CharacterSetFilterReader(input, EOF)) {
                int c;
                while ((c = reader.read()) != EOF) {
                    output.write(c);
                }
                assertEquals(STRING_FIXTURE, output.toString());
            }
        });
    }
}
