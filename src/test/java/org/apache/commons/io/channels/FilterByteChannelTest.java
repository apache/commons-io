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

package org.apache.commons.io.channels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterByteChannel}.
 */
class FilterByteChannelTest {

    private FilterByteChannel<ByteChannel> buildFilterByteChannel(final ByteChannel channel) throws IOException {
        return FilterByteChannel.forByteChannel().setChannel(channel).get();
    }

    @Test
    void testBuilderRequiresChannel() {
        assertThrows(IllegalStateException.class, () -> FilterByteChannel.forByteChannel().get());
    }

    @Test
    void testClose() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testImplementsByteChannel() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertInstanceOf(ByteChannel.class, filterChannel);
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
        assertFalse(filterChannel.isOpen());
    }

    @Test
    void testIsOpenDelegatesToChannel() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertTrue(filterChannel.isOpen());
        assertFalse(filterChannel.isOpen());
        verify(channel, times(2)).isOpen();
    }

    @Test
    void testReadDelegatesToChannel() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(8);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertEquals(8, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadPropagatesIOException() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenThrow(new IOException("read error"));
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertThrows(IOException.class, () -> filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadReturnsMinusOneAtEndOfStream() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(-1);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertEquals(-1, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testUnwrapReturnsWrappedChannel() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertSame(channel, filterChannel.unwrap());
    }

    @Test
    void testWriteDelegatesToChannel() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenReturn(16);
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertEquals(16, filterChannel.write(buffer));
        verify(channel).write(buffer);
    }

    @Test
    void testWritePropagatesIOException() throws IOException {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenThrow(new IOException("write error"));
        final FilterByteChannel<ByteChannel> filterChannel = buildFilterByteChannel(channel);
        assertThrows(IOException.class, () -> filterChannel.write(buffer));
        verify(channel).write(buffer);
    }
}
