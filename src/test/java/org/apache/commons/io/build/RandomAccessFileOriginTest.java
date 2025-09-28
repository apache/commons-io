/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractOrigin.RandomAccessFileOrigin;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessFileOrigin}.
 *
 * @see RandomAccessFile
 */
class RandomAccessFileOriginTest extends AbstractRandomAccessFileOriginTest<RandomAccessFile, RandomAccessFileOrigin> {

    @SuppressWarnings("resource")
    @Override
    protected RandomAccessFileOrigin newOriginRo() throws FileNotFoundException {
        return new RandomAccessFileOrigin(RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO));
    }

    @SuppressWarnings("resource")
    @Override
    protected RandomAccessFileOrigin newOriginRw() throws IOException {
        return new RandomAccessFileOrigin(RandomAccessFileMode.READ_WRITE.create(tempPath.resolve(FILE_NAME_RW)));
    }

    @Test
    void testClosesOrigin() throws IOException {
        final FileChannel channel = mock(FileChannel.class);
        final RandomAccessFile resource = mock(RandomAccessFile.class);
        when(resource.getChannel()).thenReturn(channel);
        final RandomAccessFileOrigin origin = new RandomAccessFileOrigin(resource);

        // These wrappers close the underlying Channel.
        origin.getInputStream().close();
        verify(channel, times(1)).close();

        origin.getReader(StandardCharsets.UTF_8).close();
        verify(channel, times(2)).close();

        origin.getChannel(ReadableByteChannel.class).close();
        verify(channel, times(3)).close();

        origin.getChannel(WritableByteChannel.class).close();
        verify(channel, times(4)).close();

        // These wrappers close the underlying RandomAccessFile.
        origin.getOutputStream().close();
        verify(resource, times(1)).close();

        origin.getWriter(StandardCharsets.UTF_8).close();
        verify(resource, times(2)).close();

        origin.getRandomAccessFile().close();
        verify(resource, times(3)).close();
    }

    @Override
    @Test
    void testGetFile() {
        // Cannot convert a Writer to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    void testGetPath() {
        // Cannot convert a Writer to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

}
