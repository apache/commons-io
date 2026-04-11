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
import java.nio.channels.SeekableByteChannel;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterSeekableByteChannel}.
 */
class FilterSeekableByteChannelTest {

    private FilterSeekableByteChannel<SeekableByteChannel> build(final SeekableByteChannel channel) throws IOException {
        return new FilterSeekableByteChannel(channel);
    }

    @Test
    void testBuilderRequiresChannel() {
        assertThrows(IllegalStateException.class, () -> FilterSeekableByteChannel.forSeekableByteChannel().get());
    }

    @Test
    void testClose() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testImplementsSeekableByteChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        assertInstanceOf(SeekableByteChannel.class, build(channel));
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
        assertFalse(filterChannel.isOpen());
    }

    @Test
    void testIsOpenDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        assertFalse(filterChannel.isOpen());
        verify(channel, times(2)).isOpen();
    }
    // -- read --

    @Test
    void testPositionDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.position()).thenReturn(42L);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertEquals(42L, filterChannel.position());
        verify(channel).position();
    }

    @Test
    void testPositionPropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.position()).thenThrow(new IOException("position error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, filterChannel::position);
        verify(channel).position();
    }

    @Test
    void testReadDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(8);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertEquals(8, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }
    // -- write --

    @Test
    void testReadPropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenThrow(new IOException("read error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadReturnsMinusOneAtEndOfStream() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(-1);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertEquals(-1, filterChannel.read(buffer));
        verify(channel).read(buffer);
    }
    // -- position() --

    @Test
    void testSetPositionDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.position(10L)).thenReturn(channel);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertSame(channel, filterChannel.position(10L));
        verify(channel).position(10L);
    }

    @Test
    void testSetPositionPropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.position(10L)).thenThrow(new IOException("position error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.position(10L));
        verify(channel).position(10L);
    }
    // -- position(long) --

    @Test
    void testSizeDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.size()).thenReturn(1024L);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertEquals(1024L, filterChannel.size());
        verify(channel).size();
    }

    @Test
    void testSizePropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.size()).thenThrow(new IOException("size error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, filterChannel::size);
        verify(channel).size();
    }
    // -- size() --

    @Test
    void testTruncateDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.truncate(512L)).thenReturn(channel);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertSame(channel, filterChannel.truncate(512L));
        verify(channel).truncate(512L);
    }

    @Test
    void testTruncatePropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.truncate(512L)).thenThrow(new IOException("truncate error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.truncate(512L));
        verify(channel).truncate(512L);
    }
    // -- truncate(long) --

    @Test
    void testUnwrapReturnsWrappedChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        assertSame(channel, build(channel).unwrap());
    }

    @Test
    void testWriteDelegatesToChannel() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenReturn(16);
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertEquals(16, filterChannel.write(buffer));
        verify(channel).write(buffer);
    }
    // -- unwrap --

    @Test
    void testWritePropagatesIOException() throws IOException {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenThrow(new IOException("write error"));
        final FilterSeekableByteChannel<SeekableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.write(buffer));
        verify(channel).write(buffer);
    }
}
