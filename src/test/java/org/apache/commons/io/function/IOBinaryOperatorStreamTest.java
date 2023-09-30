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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOBinaryOperator}.
 */
public class IOBinaryOperatorStreamTest {

    private static final IOBinaryOperator<Path> MIN_BY_IO_BO = IOBinaryOperator.minBy(IOComparatorTest.REAL_PATH_COMP);
    private static final BinaryOperator<Path> MIN_BY_BO = MIN_BY_IO_BO.asBinaryOperator();
    private static final IOBinaryOperator<Path> MAX_BY_IO_BO = IOBinaryOperator.maxBy(IOComparatorTest.REAL_PATH_COMP);
    private static final BinaryOperator<Path> MAX_BY_BO = MAX_BY_IO_BO.asBinaryOperator();
    private static final IOBinaryOperator<Path> REAL_PATH_IO_BO = (t, u) -> t.toRealPath();
    private static final BinaryOperator<Path> REAL_PATH_BO = REAL_PATH_IO_BO.asBinaryOperator();

    @Test
    public void testAsBinaryOperator() {
        assertThrows(UncheckedIOException.class,
            () -> Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(TestUtils.<Path>throwingIOBinaryOperator().asBinaryOperator()).get());
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(MAX_BY_BO).get());
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(MIN_BY_BO).get());
    }

    /**
     */
    @Test
    public void testMaxBy() {
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(MAX_BY_BO).get());
        // in-line lambda ok:
        final IOBinaryOperator<Path> binIoOp = IOBinaryOperator.maxBy((t, u) -> t.toRealPath().compareTo(u));
        final BiFunction<Path, Path, Path> asBiFunction = binIoOp.asBiFunction();
        final BinaryOperator<Path> asBinaryOperator = binIoOp.asBinaryOperator();
        assertEquals(TestConstants.ABS_PATH_B, asBiFunction.apply(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
        assertEquals(TestConstants.ABS_PATH_B, asBinaryOperator.apply(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
        //
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(asBinaryOperator).get());
        assertEquals(TestConstants.ABS_PATH_B, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B).reduce(asBinaryOperator).get());
        assertEquals(TestConstants.ABS_PATH_B, Stream.of(TestConstants.ABS_PATH_B, TestConstants.ABS_PATH_A).reduce(asBinaryOperator).get());
    }

    @Test
    public void testMinBy() {
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(MIN_BY_BO).get());
        // in-line lambda ok:
        final IOBinaryOperator<Path> binIoOp = IOBinaryOperator.minBy((t, u) -> t.toRealPath().compareTo(u));
        final BiFunction<Path, Path, Path> asBiFunction = binIoOp.asBiFunction();
        final BinaryOperator<Path> asBinaryOperator = binIoOp.asBinaryOperator();
        assertEquals(TestConstants.ABS_PATH_A, asBiFunction.apply(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
        assertEquals(TestConstants.ABS_PATH_A, asBinaryOperator.apply(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
        //
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A).reduce(asBinaryOperator).get());
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B).reduce(asBinaryOperator).get());
        assertEquals(TestConstants.ABS_PATH_A, Stream.of(TestConstants.ABS_PATH_B, TestConstants.ABS_PATH_A).reduce(asBinaryOperator).get());
    }

    @Test
    public void testReduce() throws IOException {
        // A silly example to pass in a IOBinaryOperator.
        final Path current = PathUtils.current();
        final Path expected;
        try (Stream<Path> stream = Files.list(current)) {
            expected = stream.reduce((t, u) -> {
                try {
                    return t.toRealPath();
                } catch (final IOException e) {
                    return fail(e);
                }
            }).get();
        }
        try (Stream<Path> stream = Files.list(current)) {
            assertEquals(expected, stream.reduce(REAL_PATH_BO).get());
        }
    }

}
