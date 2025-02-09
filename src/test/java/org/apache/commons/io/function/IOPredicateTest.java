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

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests {@link IOPredicate}.
 */
public class IOPredicateTest {

    /** Files::isHidden throws IOException. */
    private static final IOPredicate<Path> IS_HIDDEN = Files::isHidden;

    private static final Path PATH_FIXTURE = Paths.get("src/test/resources/org/apache/commons/io/abitmorethan16k.txt");

    private static final Object THROWING_EQUALS = new Object() {
        @Override
        public boolean equals(final Object obj) {
            throw Erase.rethrow(new IOException("Expected"));
        }
        @Override
        public int hashCode() {
            // Pair implementation with equals() even though not strictly necessary.
            return super.hashCode();
        }
    };

    private static final Predicate<Object> THROWING_UNCHECKED_PREDICATE = TestConstants.THROWING_IO_PREDICATE.asPredicate();

    private void assertThrowsChecked(final Executable executable) {
        assertThrows(IOException.class, executable);
    }

    private void assertThrowsUnchecked(final Executable executable) {
        assertThrows(UncheckedIOException.class, executable);
    }

    @Test
    public void testAndChecked() throws IOException {
        assertFalse(IS_HIDDEN.and(IS_HIDDEN).test(PATH_FIXTURE));
        assertTrue(IOPredicate.alwaysTrue().and(IOPredicate.alwaysTrue()).test(PATH_FIXTURE));
        assertFalse(IOPredicate.alwaysFalse().and(IOPredicate.alwaysTrue()).test(PATH_FIXTURE));
        assertFalse(IOPredicate.alwaysTrue().and(IOPredicate.alwaysFalse()).test(PATH_FIXTURE));
        assertFalse(IOPredicate.alwaysFalse().and(IOPredicate.alwaysFalse()).test(PATH_FIXTURE));
    }

    @Test
    public void testAndUnchecked() {
        assertThrowsUnchecked(() -> THROWING_UNCHECKED_PREDICATE.and(THROWING_UNCHECKED_PREDICATE).test(PATH_FIXTURE));
    }

    @Test
    public void testAsPredicate() throws IOException {
        new ArrayList<>().removeIf(THROWING_UNCHECKED_PREDICATE);
        final List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.removeIf(Predicate.isEqual("A"));
        assertFalse(list.contains("A"));
        list.removeIf(IOPredicate.isEqual("B").asPredicate());
        assertFalse(list.contains("B"));
        assertFalse(IS_HIDDEN.test(PATH_FIXTURE));
    }

    @Test
    public void testFalse() throws IOException {
        assertFalse(Constants.IO_PREDICATE_FALSE.test("A"));
        // Make sure we keep the argument type
        final IOPredicate<String> alwaysFalse = IOPredicate.alwaysFalse();
        assertFalse(alwaysFalse.test("A"));
        assertEquals(IOPredicate.alwaysFalse(), IOPredicate.alwaysFalse());
        assertSame(IOPredicate.alwaysFalse(), IOPredicate.alwaysFalse());
    }

    @Test
    public void testIsEqualChecked() throws IOException {
        assertThrowsChecked(() -> IOPredicate.isEqual(THROWING_EQUALS).test("B"));
        assertFalse(IOPredicate.isEqual(null).test("A"));
        assertTrue(IOPredicate.isEqual("B").test("B"));
        assertFalse(IOPredicate.isEqual("A").test("B"));
        assertFalse(IOPredicate.isEqual("B").test("A"));
    }

    @Test
    public void testIsEqualUnchecked() {
        assertThrowsUnchecked(() -> IOPredicate.isEqual(THROWING_EQUALS).asPredicate().test("B"));
        assertFalse(IOPredicate.isEqual(null).asPredicate().test("A"));
        assertTrue(IOPredicate.isEqual("B").asPredicate().test("B"));
        assertFalse(IOPredicate.isEqual("A").asPredicate().test("B"));
        assertFalse(IOPredicate.isEqual("B").asPredicate().test("A"));
    }

    @Test
    public void testNegateChecked() throws IOException {
        assertTrue(IS_HIDDEN.negate().test(PATH_FIXTURE));
        assertFalse(IOPredicate.alwaysTrue().negate().test(PATH_FIXTURE));
    }

    @Test
    public void testNegateUnchecked() {
        assertTrue(IS_HIDDEN.negate().asPredicate().test(PATH_FIXTURE));
        assertTrue(IS_HIDDEN.asPredicate().negate().test(PATH_FIXTURE));
        assertThrowsUnchecked(() -> THROWING_UNCHECKED_PREDICATE.negate().test(PATH_FIXTURE));
    }

    @Test
    public void testOrChecked() throws IOException {
        assertFalse(IS_HIDDEN.or(IS_HIDDEN).test(PATH_FIXTURE));
        assertTrue(IOPredicate.alwaysTrue().or(IOPredicate.alwaysFalse()).test(PATH_FIXTURE));
        assertTrue(IOPredicate.alwaysFalse().or(IOPredicate.alwaysTrue()).test(PATH_FIXTURE));
    }

    @Test
    public void testOrUnchecked() {
        assertFalse(IS_HIDDEN.asPredicate().or(e -> false).test(PATH_FIXTURE));
        assertThrowsUnchecked(() -> THROWING_UNCHECKED_PREDICATE.or(THROWING_UNCHECKED_PREDICATE).test(PATH_FIXTURE));
    }

    @Test
    public void testTestChecked() throws IOException {
        assertThrowsChecked(() -> TestConstants.THROWING_IO_PREDICATE.test(null));
        assertTrue(Constants.IO_PREDICATE_TRUE.test("A"));
    }

    @Test
    public void testTestUnchecked() {
        assertThrowsUnchecked(() -> THROWING_UNCHECKED_PREDICATE.test(null));
        assertTrue(Constants.IO_PREDICATE_TRUE.asPredicate().test("A"));
    }

    @Test
    public void testTrue() throws IOException {
        assertTrue(Constants.IO_PREDICATE_TRUE.test("A"));
        // Make sure we keep the argument type
        final IOPredicate<String> alwaysTrue = IOPredicate.alwaysTrue();
        assertTrue(alwaysTrue.test("A"));
        assertEquals(IOPredicate.alwaysTrue(), IOPredicate.alwaysTrue());
        assertSame(IOPredicate.alwaysTrue(), IOPredicate.alwaysTrue());
    }

}
