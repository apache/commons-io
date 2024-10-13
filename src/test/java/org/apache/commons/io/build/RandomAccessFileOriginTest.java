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

package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractOrigin.RandomAccessFileOrigin;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessFileOrigin}.
 *
 * @see RandomAccessFile
 */
public class RandomAccessFileOriginTest extends AbstractRandomAccessFileOriginTest<RandomAccessFile, RandomAccessFileOrigin> {

    @SuppressWarnings("resource")
    @Override
    protected RandomAccessFileOrigin newOriginRo() throws FileNotFoundException {
        return new RandomAccessFileOrigin(RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO));
    }

    @SuppressWarnings("resource")
    @Override
    protected RandomAccessFileOrigin newOriginRw() throws FileNotFoundException {
        return new RandomAccessFileOrigin(RandomAccessFileMode.READ_WRITE.create(FILE_NAME_RW));
    }

    @Override
    @Test
    public void testGetFile() {
        // Cannot convert a Writer to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    public void testGetPath() {
        // Cannot convert a Writer to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

}
