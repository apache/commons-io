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
import java.nio.channels.ReadableByteChannel;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterReadableByteChannel}.
 */
class FilterReadableByteChannelTest {

    private FilterReadableByteChannel<ReadableByteChannel> build(final ReadableByteChannel channel) throws IOException {
        return new FilterReadableByteChannel(channel);
    }

    @Test
    void testBuilderRequiresChannel() {
        assertThrows(IllegalStateException.class, () -> FilterReadableByteChannel.forReadableByteChannel().get());
    }

    @Test
    void testClose() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testImplementsReadableByteChannel() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        assertInstanceOf(ReadableByteChannel.class, build(channel));
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
        assertFalse(filterChannel.isOpen());
    }

    @Test
    void testIsOpenDelegatesToChannel() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        assertFalse(filterChannel.isOpen());
        verify(channel, times(2)).isOpen();
    }

    @Test
    void testReadDelegatesToChannel() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(8);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertEquals(8, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadMultipleCalls() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(4, 4, -1);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertEquals(4, filterChannel.read(buffer));
        assertEquals(4, filterChannel.read(buffer));
        assertEquals(-1, filterChannel.read(buffer));
        verify(channel, times(3)).read(buffer);
    }

    @Test
    void testReadPropagatesIOException() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenThrow(new IOException("read error"));
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadReturnsMinusOneAtEndOfStream() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(-1);
        final FilterReadableByteChannel<ReadableByteChannel> filterChannel = build(channel);
        assertEquals(-1, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testUnwrapReturnsWrappedChannel() throws IOException {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        assertSame(channel, build(channel).unwrap());
    }
}
