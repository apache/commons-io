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
import java.nio.channels.WritableByteChannel;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterWritableByteChannel}.
 */
class FilterWritableByteChannelTest {

    private FilterWritableByteChannel<WritableByteChannel> build(final WritableByteChannel channel) throws IOException {
        return new FilterWritableByteChannel(channel);
    }

    @Test
    void testBuilderRequiresChannel() {
        assertThrows(IllegalStateException.class, () -> FilterWritableByteChannel.forWritableByteChannel().get());
    }

    @Test
    void testClose() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testImplementsWritableByteChannel() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        assertInstanceOf(WritableByteChannel.class, build(channel));
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
        assertFalse(filterChannel.isOpen());
    }

    @Test
    void testIsOpenDelegatesToChannel() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertTrue(filterChannel.isOpen());
        assertFalse(filterChannel.isOpen());
        verify(channel, times(2)).isOpen();
    }

    @Test
    void testUnwrapReturnsWrappedChannel() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        assertSame(channel, build(channel).unwrap());
    }

    @Test
    void testWriteDelegatesToChannel() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenReturn(16);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertEquals(16, filterChannel.write(buffer));
        verify(channel).write(buffer);
    }

    @Test
    void testWriteMultipleCalls() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenReturn(8, 8);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertEquals(8, filterChannel.write(buffer));
        assertEquals(8, filterChannel.write(buffer));
        verify(channel, times(2)).write(buffer);
    }

    @Test
    void testWritePropagatesIOException() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenThrow(new IOException("write error"));
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertThrows(IOException.class, () -> filterChannel.write(buffer));
        verify(channel).write(buffer);
    }

    @Test
    void testWriteReturnsZeroOnFullBuffer() throws IOException {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        final ByteBuffer buffer = ByteBuffer.allocate(0);
        when(channel.write(buffer)).thenReturn(0);
        final FilterWritableByteChannel<WritableByteChannel> filterChannel = build(channel);
        assertEquals(0, filterChannel.write(buffer));
        verify(channel).write(buffer);
    }
}
