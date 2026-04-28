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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Tests {@link CloseShieldChannel} wrapping a {@link FilterByteChannel}.
 */
class CloseShieldChannelFilterByteChannelTest {

    /** A FilterByteChannel wrapping the mock. */
    private FilterByteChannel<ByteChannel> filterChannel;

    /** The innermost mock delegate. */
    private ByteChannel mockChannel;

    /** The CloseShieldChannel wrapping the FilterByteChannel. */
    private ByteChannel shield;

    @BeforeEach
    void setUp() throws IOException {
        mockChannel = mock(ByteChannel.class);
        filterChannel = new FilterByteChannel<>(mockChannel);
        shield = CloseShieldChannel.wrap(filterChannel);
    }

    @Test
    void testCloseDoesNotCloseFilterByteChannel() throws IOException {
        when(mockChannel.isOpen()).thenReturn(true);
        shield.close();
        // The FilterByteChannel (and thus the mock) must not have been closed.
        verify(mockChannel, never()).close();
        assertTrue(filterChannel.isOpen(), "FilterByteChannel must still be open");
    }

    @Test
    void testCloseIsIdempotent() throws IOException {
        shield.close();
        shield.close();
        assertFalse(shield.isOpen());
        verify(mockChannel, never()).close();
    }

    @Test
    void testDoubleWrapReturnsSameShield() {
        final ByteChannel shield2 = CloseShieldChannel.wrap(shield);
        assertSame(shield, shield2, "wrapping an existing shield must return the same proxy");
    }

    @Test
    void testFilterByteChannelUsableAfterShieldClose() throws IOException {
        when(mockChannel.isOpen()).thenReturn(true);
        shield.close();
        // The shield is closed, but the underlying FilterByteChannel must still work.
        assertTrue(filterChannel.isOpen());
        final ByteBuffer buf = ByteBuffer.allocate(4);
        when(mockChannel.read(buf)).thenReturn(4);
        assertEquals(4, filterChannel.read(buf));
        verify(mockChannel).read(buf);
    }

    @Test
    void testIsOpenDelegatesBeforeShieldClose() throws IOException {
        when(mockChannel.isOpen()).thenReturn(true, false);
        assertTrue(shield.isOpen(), "reflects open delegate");
        assertFalse(shield.isOpen(), "reflects closed delegate");
        verify(mockChannel, times(2)).isOpen();
    }

    @Test
    void testIsOpenReturnsFalseAfterShieldClose() throws IOException {
        when(mockChannel.isOpen()).thenReturn(true);
        shield.close();
        assertFalse(shield.isOpen(), "shield is closed so isOpen must return false");
        // isOpen must NOT be forwarded to the delegate after the shield is closed.
        verify(mockChannel, never()).isOpen();
    }

    @Test
    void testReadDelegatesToFilterByteChannel() throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(16);
        when(mockChannel.read(buf)).thenReturn(8);
        when(mockChannel.isOpen()).thenReturn(true);
        assertEquals(8, shield.read(buf));
        verify(mockChannel).read(buf);
    }

    @Test
    void testReadIOExceptionPropagates() throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(8);
        when(mockChannel.isOpen()).thenReturn(true);
        when(mockChannel.read(buf)).thenThrow(new IOException("read failure"));
        assertThrows(IOException.class, () -> shield.read(buf));
        verify(mockChannel).read(buf);
    }

    @Test
    void testReadThrowsClosedChannelExceptionAfterShieldClose() throws IOException {
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.read(ByteBuffer.allocate(8)));
        verify(mockChannel, never()).read(ArgumentMatchers.any());
    }

    @Test
    void testShieldImplementsByteChannel() {
        assertInstanceOf(ByteChannel.class, shield);
    }

    @Test
    void testWriteDelegatesToFilterByteChannel() throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(16);
        when(mockChannel.write(buf)).thenReturn(16);
        when(mockChannel.isOpen()).thenReturn(true);
        assertEquals(16, shield.write(buf));
        verify(mockChannel).write(buf);
    }

    @Test
    void testWriteIOExceptionPropagates() throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(8);
        when(mockChannel.isOpen()).thenReturn(true);
        when(mockChannel.write(buf)).thenThrow(new IOException("write failure"));
        assertThrows(IOException.class, () -> shield.write(buf));
        verify(mockChannel).write(buf);
    }

    @Test
    void testWriteThrowsClosedChannelExceptionAfterShieldClose() throws IOException {
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.write(ByteBuffer.allocate(8)));
        verify(mockChannel, never()).write(ArgumentMatchers.any());
    }
}
