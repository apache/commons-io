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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileEntry}.
 */
public class FileEntryTest {

    @Test
    public void testSerializable() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(fe.getChildren(), SerializationUtils.roundtrip(fe).getChildren());
        assertEquals(fe.getClass(), SerializationUtils.roundtrip(fe).getClass());
        assertEquals(fe.getFile(), SerializationUtils.roundtrip(fe).getFile());
        assertEquals(fe.getLastModified(), SerializationUtils.roundtrip(fe).getLastModified());
        assertEquals(fe.getLastModifiedFileTime(), SerializationUtils.roundtrip(fe).getLastModifiedFileTime());
        assertEquals(fe.getLength(), SerializationUtils.roundtrip(fe).getLength());
        assertEquals(fe.getLevel(), SerializationUtils.roundtrip(fe).getLevel());
        assertEquals(fe.getName(), SerializationUtils.roundtrip(fe).getName());
        assertEquals(fe.getParent(), SerializationUtils.roundtrip(fe).getParent());
    }
}
