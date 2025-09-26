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

import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.io.build.AbstractOrigin.ChannelOrigin;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ChannelOriginTest extends AbstractOriginTest<Channel, ChannelOrigin> {
    @Override
    protected ChannelOrigin newOriginRo() throws IOException {
        return new ChannelOrigin(Files.newByteChannel(Paths.get(FILE_NAME_RO), Collections.singleton(READ)));
    }

    @Override
    protected ChannelOrigin newOriginRw() throws IOException {
        return new ChannelOrigin(Files.newByteChannel(
                tempPath.resolve(FILE_NAME_RW),
                new HashSet<>(Arrays.asList(StandardOpenOption.READ, StandardOpenOption.WRITE))));
    }

    @Override
    protected void resetOriginRw() throws IOException {
        // Reset the file
        final Path rwPath = tempPath.resolve(FILE_NAME_RW);
        Files.write(rwPath, ArrayUtils.EMPTY_BYTE_ARRAY, StandardOpenOption.CREATE);
    }

    @Override
    @Test
    void testGetFile() {
        // A FileByteChannel cannot be converted into a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    void testGetPath() {
        // A FileByteChannel cannot be converted into a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

    @Override
    @Test
    void testGetRandomAccessFile() {
        // A FileByteChannel cannot be converted into a RandomAccessFile.
        assertThrows(UnsupportedOperationException.class, super::testGetRandomAccessFile);
    }

    @Override
    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    void testGetRandomAccessFile(final OpenOption openOption) {
        // A FileByteChannel cannot be converted into a RandomAccessFile.
        assertThrows(UnsupportedOperationException.class, () -> super.testGetRandomAccessFile(openOption));
    }

    @Test
    void testUnsupportedOperations_ReadableByteChannel() {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final ChannelOrigin origin = new ChannelOrigin(channel);
        assertThrows(UnsupportedOperationException.class, origin::getOutputStream);
        assertThrows(UnsupportedOperationException.class, () -> origin.getWriter(null));
        assertThrows(UnsupportedOperationException.class, () -> origin.getChannel(WritableByteChannel.class));
    }

    @Test
    void testUnsupportedOperations_WritableByteChannel() {
        final Channel channel = mock(WritableByteChannel.class);
        final ChannelOrigin origin = new ChannelOrigin(channel);
        assertThrows(UnsupportedOperationException.class, origin::getInputStream);
        assertThrows(UnsupportedOperationException.class, () -> origin.getReader(null));
        assertThrows(UnsupportedOperationException.class, () -> origin.getChannel(ReadableByteChannel.class));
    }
}
