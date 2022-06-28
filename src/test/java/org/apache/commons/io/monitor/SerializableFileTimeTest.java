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
package org.apache.commons.io.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SerializableFileTime}.
 */
public class SerializableFileTimeTest {

    @Test
    public void testSerializable() throws IOException {
        final SerializableFileTime expected = new SerializableFileTime(Files.getLastModifiedTime(PathUtils.current()));
        final SerializableFileTime actual = SerializationUtils.roundtrip(expected);
        assertEquals(expected, actual);
        final FileTime expectedFt = expected.unwrap();
        assertEquals(expectedFt, actual.unwrap());
        assertEquals(0, actual.compareTo(expectedFt));
        assertEquals(expectedFt.hashCode(), actual.hashCode());
        assertEquals(expectedFt.toInstant(), actual.toInstant());
        assertEquals(expectedFt.toMillis(), actual.toMillis());
        assertEquals(expectedFt.toString(), actual.toString());
    }

}
