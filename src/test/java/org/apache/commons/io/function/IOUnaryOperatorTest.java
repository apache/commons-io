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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOUnaryOperator}.
 */
public class IOUnaryOperatorTest {

    @Test
    public void testAsUnaryOperator() {
        final List<Path> list = Arrays.asList(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A);
        final IOUnaryOperator<Path> throwingIOUnaryOperator = TestUtils.throwingIOUnaryOperator();
        assertThrows(UncheckedIOException.class, () -> list.replaceAll(throwingIOUnaryOperator.asUnaryOperator()));
        assertEquals("a", Optional.of("a").map(IOUnaryOperator.identity().asUnaryOperator()).get());
        assertEquals("a", Optional.of("a").map(IOUnaryOperator.identity().asFunction()).get());
    }

    @Test
    public void testIdentity() throws IOException {
        assertEquals(IOUnaryOperator.identity(), IOUnaryOperator.identity());
        final IOUnaryOperator<byte[]> identityFunction = IOUnaryOperator.identity();
        final byte[] buf = {(byte) 0xa, (byte) 0xb, (byte) 0xc};
        assertEquals(buf, identityFunction.apply(buf));
        assertArrayEquals(buf, identityFunction.apply(buf));
    }

}
